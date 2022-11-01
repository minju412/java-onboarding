package onboarding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Problem7 {

	public static final int MAX_RETURN_SIZE = 5;
	public static final int BOTH_KNOW_SCORE = 10;
	public static final int TIMELINE_VISIT_SCORE = 1;

	public static final int EXCEPTION = -1;

	public static List<String> solution(String user, List<List<String>> friends, List<String> visitors) {
		List<String> answer = new ArrayList<>(Collections.emptyList());
		Map<String, Integer> nameAndScores = new HashMap<>(); // [사람 이름, 추천 점수]를 담을 HashMap
		Set<String> notYetFriendWithUser; // 추천 친구 대상 set (사용자와 아직 친구가 아닌 사람들)
		Set<String> alreadyFriendWithUser = new HashSet<>(); // 사용자와 이미 친구인 사람들 set

		if (checkRestrictions(user, friends, visitors) == EXCEPTION) {
			return answer;
		}

		notYetFriendWithUser = initNotYetFriendWithUser(user, friends, visitors, alreadyFriendWithUser);
		alreadyFriendWithUser.remove(user); // 사용자 본인은 제외

		// HashMap 초기화
		for (String friend : notYetFriendWithUser) {
			nameAndScores.put(friend, 0);
		}

		// 추천 점수 계산
		calcScore(friends, visitors, nameAndScores, notYetFriendWithUser, alreadyFriendWithUser);

		// 추천 점수(value)가 0점인 사람(key)은 제외
		Set<String> zeroPoints = new HashSet<>();
		for (Map.Entry<String, Integer> entry : nameAndScores.entrySet()) {
			if (entry.getValue() == 0) {
				zeroPoints.add(entry.getKey());
			}
		}
		zeroPoints.forEach(k -> nameAndScores.remove(k));

		// 정렬
		List<Map.Entry<String, Integer>> sortedFriends = sortFriends(nameAndScores);

		// 추천 친구 이름을 리스트에 담아 반환
		for (Map.Entry<String, Integer> stringIntegerEntry : sortedFriends) {
			answer.add(stringIntegerEntry.getKey());
		}

		return answer;
	}

	/**
	 * 제한사항을 위배했는지 체크하는 메서드
	 * @param user
	 * @param friends
	 * @param visitors
	 * @return
	 */
	private static Integer checkRestrictions(String user, List<List<String>> friends, List<String> visitors) {
		if (!isUserRangeValid(user)) {
			return EXCEPTION;
		}
		if (!isFriendsRangeValid(friends)) {
			return EXCEPTION;
		}
		if (!isIdRangeValid(friends)) {
			return EXCEPTION;
		}
		if (!isVisitorsRangeValid(visitors)) {
			return EXCEPTION;
		}
		if (!isIdLowerCase(friends)) {
			return EXCEPTION;
		}
		return 0;
	}

	// user 의 길이가 1 이상 30 이하가 아닌 경우 예외
	private static boolean isUserRangeValid(String user) {
		if (user.length() < 1 || user.length() > 30) {
			return false;
		}
		return true;
	}

	// friends 의 길이 1 이상 10,000 이하가 아닌 경우 예외
	private static boolean isFriendsRangeValid(List<List<String>> friends) {
		if (friends.size() < 1 || friends.size() > 10000) {
			return false;
		}
		return true;
	}

	// 사용자 아이디의 길이가 1 이상 30 이하가 아닌 경우 예외
	private static boolean isIdRangeValid(List<List<String>> friends) {
		for (List<String> friend : friends) {
			for (String id : friend) {
				if (id.length() < 1 || id.length() > 30) {
					return false;
				}
			}
		}
		return true;
	}

	// visitors 의 길이가 0 이상 10,000 이하가 아닌 경우 예외
	private static boolean isVisitorsRangeValid(List<String> visitors) {
		if (visitors.size() > 10000) {
			return false;
		}
		return true;
	}

	// 사용자 아이디가 알파벳 소문자로만 이루어져 있지 않은 경우 예외
	private static boolean isIdLowerCase(List<List<String>> friends) {
		for (List<String> friend : friends) {
			for (String id : friend) {
				if (!id.equals(id.toLowerCase())) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 추천 점수 순으로 정렬하고, 추천 점수가 같은 경우는 이름순으로 정렬하는 메서드
	 * 최대 5명만 리턴한다.
	 * @param nameAndScores
	 * @return
	 */
	private static List<Map.Entry<String, Integer>> sortFriends(Map<String, Integer> nameAndScores) {

		List<Map.Entry<String, Integer>> friends = new LinkedList<>(nameAndScores.entrySet());
		friends.sort((o1, o2) -> {
			// value 값 내림차순 정렬
			int comparison = (o1.getValue() - o2.getValue()) * -1;
			// value 값이 같으면 key 값 오름차순 정렬
			return comparison == 0 ? o1.getKey().compareTo(o2.getKey()) : comparison;
		});

		// 정렬 후 5명 초과이면 최대 5명까지만
		while (friends.size() > MAX_RETURN_SIZE) {
			friends.remove(friends.size() - 1);
		}
		return friends;
	}

	/**
	 * 추천 점수를 계산하는 메서드
	 * @param friends
	 * @param visitors
	 * @param nameAndScores
	 * @param notYetFriendWithUser
	 * @param alreadyFriendWithUser
	 */
	private static void calcScore(List<List<String>> friends, List<String> visitors, Map<String, Integer> nameAndScores,
		Set<String> notYetFriendWithUser, Set<String> alreadyFriendWithUser) {
		calcBothKnowScore(friends, nameAndScores, notYetFriendWithUser, alreadyFriendWithUser);
		calcVisitScore(visitors, nameAndScores, notYetFriendWithUser);
	}

	/**
	 * (사용자의 타임 라인에 방문한 횟수 * 1점) 을 계산하는 메서드
	 * @param visitors
	 * @param nameAndScores
	 * @param notYetFriendWithUser
	 */
	private static void calcVisitScore(List<String> visitors, Map<String, Integer> nameAndScores,
		Set<String> notYetFriendWithUser) {
		for (String visitor : visitors) {
			if (notYetFriendWithUser.contains(visitor)) {
				nameAndScores.put(visitor, nameAndScores.get(visitor) + TIMELINE_VISIT_SCORE); // visitor 의 친구 점수 + 1
			}
		}
	}

	/**
	 * (사용자와 함께 아는 친구 수 * 10점) 을 계산하는 메서드
	 * @param friends
	 * @param nameAndScores
	 * @param notYetFriendWithUser
	 * @param alreadyFriendWithUser
	 */
	private static void calcBothKnowScore(List<List<String>> friends, Map<String, Integer> nameAndScores,
		Set<String> notYetFriendWithUser, Set<String> alreadyFriendWithUser) {
		for (List<String> friend : friends) {
			for (String alreadyFriend : alreadyFriendWithUser) {
				for (String person : friend) {
					if (friend.contains(alreadyFriend) && notYetFriendWithUser.contains(
						person)) { // 사용자와 함께 아는 친구가 있다면
						nameAndScores.put(person, nameAndScores.get(person) + BOTH_KNOW_SCORE); // person 의 친구 점수 + 10
					}
				}
			}
		}
	}

	/**
	 * 친구 점수를 계산해야 하는 사람들로 notYetFriendWithUser 을 초기화하는 메서드
	 * (사용자 본인이거나 이미 친구인 사람들은 친구 점수를 계산할 필요가 없다.)
	 * (사용자 본인 혹은 이미 친구인 사람들은 alreadyFriendWithUser 에 담는다.)
	 * @param user
	 * @param friends
	 * @param visitors
	 * @param alreadyFriendWithUser
	 */
	private static Set<String> initNotYetFriendWithUser(String user, List<List<String>> friends, List<String> visitors,
		Set<String> alreadyFriendWithUser) {
		Set<String> notYetFriendWithUser = new HashSet<>();

		// friendSet 초기화
		for (List<String> friend : friends) {
			notYetFriendWithUser.addAll(friend);
			if (friend.contains(user)) { // 사용자 본인 & 사용자와 이미 친구인 사람들
				alreadyFriendWithUser.addAll(friend);
			}
		}
		notYetFriendWithUser.addAll(visitors);

		// 사용자 본인 & 사용자와 이미 친구인 사람들은 notYetFriendWithUser 에서 제외
		for (String s : alreadyFriendWithUser) {
			notYetFriendWithUser.remove(s);
		}
		return notYetFriendWithUser;
	}
}
