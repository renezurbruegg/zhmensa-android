package com.mensa.zhmensa.services;


import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.poll.Poll;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import kotlin.Function;
import kotlin.jvm.functions.Function1;

@SuppressWarnings("HardCodedStringLiteral")
public class PollApi {
      private static final String BASE_URL = "http://mensazh.vsos.ethz.ch:8080/api/polls";
      private static final String CREATE = "/create";


    public static class PollApiResponse {
          public final boolean error;
          public final String msg;
          public final Throwable throwable;

          public PollApiResponse(boolean error, String msg, Throwable throwable) {
              this.error = error;
              this.msg = msg;
              this.throwable = throwable;
          }
      }



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

      public static void updatePoll(Poll poll, final Function1<PollApiResponse, Void> callback, Context ctx) {
          JsonObject payload = convertPollToJsonObj(poll);

          Log.d("updatePoll", payload.toString());

          try {
              HttpUtils.post(BASE_URL + "/update/" + poll.id, payload, ctx,  new AsyncHttpResponseHandler() {
                  @Override
                  public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                      String jsonString = new String(responseBody);
                      Log.e("update poll", jsonString);

                      callback.invoke(new PollApiResponse(false, "success", null));
                      Log.e("sucesss.", "returned: " + new String(responseBody));
                  }

                  @Override
                  public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                      Log.e("error.", "returned: " + new String(responseBody));
                      callback.invoke(new PollApiResponse(true, new String(responseBody), error));
                  }
              });
          } catch (UnsupportedEncodingException e) {
              callback.invoke(new PollApiResponse(true, "UnsupportedEncodingException", e));
          }
      }

      // TODO add PollApiResponse Logic

      public static void addNewPollToApi(Poll poll, final Function1<JsonElement, Void> callback, Context ctx) {

          JsonObject payload = convertPollToJsonObj(poll);
          Log.d("addNewPollToAPi", "json: "+ payload.toString());
          try {
              HttpUtils.post(BASE_URL + CREATE, payload, ctx,  new AsyncHttpResponseHandler() {
                  @Override
                  public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                      String jsonString = new String(responseBody);
                      callback.invoke(new JsonParser().parse(jsonString));
                      Log.e("sucesss.", "returned: " + new String(responseBody));
                  }

                  @Override
                  public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                      Log.e("error.", "returned: " + new String(responseBody));
                      callback.invoke(null);
                  }
              });
          } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
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
                        callback.invoke(new PollApiResponse(false, jsonString, null));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e("error.", "returned: " + new String(responseBody));
                        callback.invoke(new PollApiResponse(true, new String(responseBody), error));
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
      public static void voteForPollOption(Context ctx, Poll poll, Poll.PollOption option, boolean firstVote, final Function1<PollApiResponse, Void> callback) {
          performVoteAction(ctx, poll, option, firstVote, callback, false);
      }

      private static Poll parseJsonEntryToPoll(JsonObject json, Context ctx) {

          Poll poll =  new Poll(json.get("title").getAsString(), "id", Mensa.MenuCategory.from(json.get("mealType").getAsString()), Mensa.Weekday.of(json.get("weekday").getAsNumber().intValue()));
          poll.weekday = Mensa.Weekday.of(json.get("weekday").getAsNumber().intValue());
          poll.menuCategory = Mensa.MenuCategory.from(json.get("mealType").getAsString());
          poll.votes = json.get("votecount").getAsNumber().intValue();
          poll.id = json.get("id").getAsString();
         // poll.id =

        //  Mensa m = null;

          for(JsonElement pollElement : json.getAsJsonArray("options")) {
              JsonObject entry = pollElement.getAsJsonObject();
              String mensaId = entry.get("mensaId").getAsString();
              int votes = entry.get("votes").getAsNumber().intValue();
              poll.addNewOption(entry.get("menuId").getAsString(), mensaId, votes, ctx);


           /*   if(m == null || !m.getUniqueId().equals(mensaId)) {
                  m = MensaManager.getMensaForId(mensaId);
              }

              MenuFilter filter = new MenuIdFilter(entry.get("menuId").getAsString());
              if(m != null) {
                  Poll.PollOption option = new Poll.PollOption(MensaManager.getFirstMenuForFiter(mensaId, poll.menuCategory, poll.weekday, filter), mensaId);
                  //TODO option.votes = entry.get("vote")

              }*/
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


    public static void getPollsForId(Collection<String> ids, final Context ctx, final Function1<List<Poll>, Void> callback) {

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
            e.printStackTrace();
    }
}
}
