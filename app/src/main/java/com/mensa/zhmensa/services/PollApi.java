package com.mensa.zhmensa.services;


import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.poll.Poll;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import kotlin.jvm.functions.Function1;

/**
 * Helper Function that performs calls to the mensazurich.ch Poll API
 */
class PollApi {
      private static final String BASE_URL = "http://mensazh.vsos.ethz.ch:8080/api/polls";
      private static final String CREATE = "/create";


    /**
     * POJO to provide information about a Server response
     */
    static class PollApiResponse {
          final boolean error;
          final String msg;
          // Null if there was no error
          final Throwable throwable;

          PollApiResponse(boolean error, String msg, Throwable throwable) {
              this.error = error;
              this.msg = msg;
              this.throwable = throwable;
          }

        PollApiResponse(String msg) {
              this(false, msg, null);
        }
      }


    /**
     * Converts a Poll Object to a JsonObject that can be sent to the Server as payload
     * @param poll
     * @return
     */
      private static JsonObject convertPollToJsonObj(Poll poll) {
          JsonObject payload = new JsonObject();
          payload.addProperty("title", poll.label);
          payload.addProperty("weekday", poll.weekday.day);
          payload.addProperty("mealType", poll.menuCategory.toString());

          JsonArray options = new JsonArray();

          for(Poll.PollOption pOption: poll.getOptions()) {
              JsonObject option = new JsonObject();
              option.addProperty("mensaId", pOption.id.mensaName);
              option.addProperty("menuId", pOption.id.menuId);
              options.add(option);
          }

          payload.add("options", options);
          return payload;
      }

    /**
     * Updates a given poll on the Server
     * @param poll
     * @param callback
     * @param ctx
     */
      static void updatePoll(Poll poll, final Function1<PollApiResponse, Void> callback, final Context ctx) {
          JsonObject payload = convertPollToJsonObj(poll);

          Log.d("updatePoll", payload.toString());

          try {
              HttpUtils.post(BASE_URL + "/update/" + poll.id, payload, ctx,  new AsyncHttpResponseHandler() {
                  @Override
                  public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                      callback.invoke(new PollApiResponse(ctx.getString(R.string.update_successfully)));
                  }

                  @Override
                  public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                      callback.invoke(new PollApiResponse(true, (responseBody == null ? "" : new String(responseBody)), error));
                  }
              });
          } catch (UnsupportedEncodingException e) {
              callback.invoke(new PollApiResponse(true, "Unsupported Encoding", e));
          }
      }

    /**
     *  Adds a new Poll and returns the ID given by the server to the callback function
     * @param poll
     * @param callback
     * @param ctx
     */
      public static void addNewPollToApi(Poll poll, final Function1<JsonElement, Void> callback, Context ctx) {

          JsonObject payload = convertPollToJsonObj(poll);
          Log.d("addNewPollToAPi", "json: "+ payload.toString());
          try {
              HttpUtils.post(BASE_URL + CREATE, payload, ctx,  new AsyncHttpResponseHandler() {
                  @Override
                  public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                      String jsonString = new String(responseBody);
                      callback.invoke(new JsonParser().parse(jsonString));

                  }

                  @Override
                  public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                      callback.invoke(null);
                  }
              });
          } catch (UnsupportedEncodingException e) {
             Log.e("addNewPollToApi", "UnsupportedEncodingException", e);
          }
      }


    public static void removeVoteForPollOption(Context ctx, Poll poll, Poll.PollOption option, Function1<PollApiResponse, Void> callback) {
        performVoteAction(ctx, poll, option, false, callback, true);
    }

        private static void performVoteAction(Context ctx, Poll poll, Poll.PollOption option, boolean firstVote, final Function1<PollApiResponse, Void> callback, boolean removeVote){
            JsonObject payload = new JsonObject();
            JsonArray votes = new JsonArray();
            JsonObject vote = new JsonObject();
            vote.addProperty("mensaId", option.id.mensaName);
            vote.addProperty("menuId", option.id.menuId);
            vote.addProperty("voteType", removeVote ? "negative" : "positive");
            votes.add(vote);

            payload.add("votes", votes);
            payload.addProperty("update", !firstVote);

            try {
                HttpUtils.post(BASE_URL + "/vote/" + poll.id, payload, ctx,  new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String jsonString = new String(responseBody);
                        callback.invoke(new PollApiResponse(jsonString));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e("error.", "returned: " + new String(responseBody));
                        callback.invoke(new PollApiResponse(true, new String(responseBody), error));
                    }
                });
            } catch (UnsupportedEncodingException e) {
                Log.e("performVoteAction", "UnsupportedEncodingException", e);
            }
        }




      static void voteForPollOption(Context ctx, Poll poll, Poll.PollOption option, boolean firstVote, final Function1<PollApiResponse, Void> callback) {
          performVoteAction(ctx, poll, option, firstVote, callback, false);
      }

    /**
     * Converts a JSON Object to a Poll Object
     * @param json
     * @param ctx
     * @return
     */
      private static Poll parseJsonEntryToPoll(JsonObject json, Context ctx) {

          Poll poll =  new Poll(json.get("title").getAsString(), "id", Mensa.MenuCategory.from(json.get("mealType").getAsString()), Mensa.Weekday.of(json.get("weekday").getAsNumber().intValue()), json.get("creationdate").getAsString());
          poll.weekday = Mensa.Weekday.of(json.get("weekday").getAsNumber().intValue());
          poll.menuCategory = Mensa.MenuCategory.from(json.get("mealType").getAsString());
          poll.votes = json.get("votecount").getAsNumber().intValue();
          poll.id = json.get("id").getAsString();

          for(JsonElement pollElement : json.getAsJsonArray("options")) {
              JsonObject entry = pollElement.getAsJsonObject();
              String mensaId = entry.get("mensaId").getAsString();
              int votes = entry.get("votes").getAsNumber().intValue();
              poll.addNewOption(entry.get("menuId").getAsString(), mensaId, votes, ctx);
          }

          return poll;
      }


      private static List<Poll> parseToPollList(String jsonString, Context ctx) {
          JsonElement obj = new JsonParser().parse(jsonString);
          JsonArray array = obj.getAsJsonObject().getAsJsonArray("polls");

          List<Poll> list = new ArrayList<>();

          for (JsonElement jsonElement : array) {
              JsonObject entry = jsonElement.getAsJsonObject();
              Poll p = parseJsonEntryToPoll(entry, ctx);
              list.add(p);
              Log.d("Polls:", p.toString());
          }

          return list;
      }


    static void getPollsForId(Collection<String> ids, final Context ctx, final Function1<List<Poll>, Void> callback) {

        JsonObject payload = new JsonObject();
        JsonArray array = new JsonArray();

        for (String id : ids) {
            Log.d("adding to entry", "id "  + id);
            array.add(id);
        }
        payload.add("ids", array);


        try {
            HttpUtils.post(BASE_URL, payload, ctx,  new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    callback.invoke(parseToPollList(new String(responseBody), ctx));
                    Log.d("sucesss.", "returned: " + new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("error.", "returned: " , error);
                    callback.invoke(null);
                }
            });
        } catch (UnsupportedEncodingException e) {
            Log.e("getPollsForId", "UnsupportedEncodingException", e);
    }
}
}
