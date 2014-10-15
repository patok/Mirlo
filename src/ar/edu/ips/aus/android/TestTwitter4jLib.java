package ar.edu.ips.aus.android;

import static java.lang.System.out;

import java.util.List;
import java.util.Map;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TestTwitter4jLib {

	public static void main(String[] args) {
		TwitterTest tt = new TestTwitter4jLib.TwitterTest();
		try {
			tt.init();

			out.println(tt.buildUsernameOutput());

			out.println(tt.buildRateLimitsOutput());

			out.println(tt.buildHomeTimelineOutput());

		} catch (TwitterException e) {
			System.out.println("An error has happened.");
			e.printStackTrace();
		}
	}

	public static class TwitterTest {
		private final String TIMELINE_SEPARATOR = "---------------------------------------";
		private final String SCREEN_SEPARATOR = "========================================";
		private Twitter twitter;
		private String screenName;

		public List<Status> getHomeTimeLine() throws TwitterException {
			return twitter.getHomeTimeline();
		}

		public String buildHomeTimelineOutput() throws TwitterException {
			List<twitter4j.Status> statuses = twitter.getHomeTimeline();
			StringBuilder buff = new StringBuilder();
			buff.append(SCREEN_SEPARATOR).append("\n");
			for (twitter4j.Status status : statuses) {
				buff.append(status.getUser().getScreenName()).append(" :: ")
						.append(status.getText()).append("\n");
				buff.append(TIMELINE_SEPARATOR).append("\n");
			}

			return buff.toString();
		}

		public String buildRateLimitsOutput() throws TwitterException {
			Map<String, RateLimitStatus> rateLimitStatus = twitter
					.getRateLimitStatus("search");
			RateLimitStatus searchTweetsRateLimit = rateLimitStatus
					.get("/search/tweets");
			StringBuilder buff = new StringBuilder();
			buff.append(SCREEN_SEPARATOR).append("\n");
			buff.append("rate limit :" + rateLimitStatus).append("\n");
			buff.append("search rate limit :" + searchTweetsRateLimit).append(
					"\n");
			buff.append(SCREEN_SEPARATOR).append("\n");

			return buff.toString();
		}

		public String buildUsernameOutput() throws TwitterException {
			User user = twitter.showUser(screenName);
			StringBuilder buff = new StringBuilder();
			buff.append(SCREEN_SEPARATOR).append("\n");
			buff.append("Usuario :" + user.toString()).append("\n");
			buff.append(SCREEN_SEPARATOR).append("\n");

			return buff.toString();
		}

		public void init() {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setOAuthConsumerKey("RYur1bWtcK187eci5c5X13dNe")
					.setOAuthConsumerSecret(
							"1uMhV4ap3xJ7gj0mF7eFrQIxbo2uqHKtNv37lOrXjLDZPWzOhN")
					.setOAuthAccessToken(
							"188022802-NlPKGFo5i8id6BRgyPjIoYZTYL2KTfohEqGioNM6")
					.setOAuthAccessTokenSecret(
							"2OOBlYMYtTg51jDtdlU7CxerP6noc9vkAldb8I8X5HQNQ");

			screenName = "ips_aus";

			twitter = new TwitterFactory(cb.build()).getInstance();
		}

	}

}
