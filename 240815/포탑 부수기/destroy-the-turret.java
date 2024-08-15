import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.Delayed;

/**
 * 
 * 시작시간: 오후 2시 58분
 * 문제 해석)
 * - N * M 격자, 모든 위치에는 포탑이 존재 (NM개)
 * - 포탑 공격력 존재, 상황에 따라 변동
 * - 공격력이 0 이하가 된 경우, 포탑은 부서지며 공격 X
 * - 최초에도 공격력이 0인 포탑 존재 가능
 * - 하나의 턴은 다음의 4가지 액션을 순서대로 수행, 총 K번 반복
 * - 만약 부서지지 않은 포탑이 1개가 된다면 그 중시 턴 중지
 * 
 * 1. 공격자 선정
 * - 부서지지 않은 포탑 중 ** 가장 약한 포탑 **이 공격자로 선정
 * - 공격자로 선정되면 가장 약한 포탑이므로, 핸디캡 적용되어 N+M 만큼 공격력 증가
 * - 가장 약한 포탑은 다음 기준으로 선정
 * 		ㄱ. 공격력이 가장 낮은 포탑이 가장 약한 포탑 (0 제외)
 * 		ㄴ. 그런 포탑이 2개 이상인 경우, 가장 최근에 공격한 포탑이 가장 약한 포탑 (모든 포탑은 첫 턴에 모두 공격한 경험이 있음)
 * 		ㄷ. 그런 포탑이 2개 이상인 경우, 각 포탑 위치의 행과 열의 합이 가장 큰 포탑이 가장 약한 포탑
 * 		ㄹ. 그런 포탑이 2개 이상인 경우, 각 포탑 위치의 열 값이 가장 큰 포탑이 가장 약한 포탑
 * 
 * => 공격자 선정이 까다로우면 우선 순위큐에 넣어놔도 될듯?
 * 
 * 2. 공격자의 공격
 * - 선정된 공격자는 **자신을 제외**한 **가장 강한 포탑**을 공격
 * - 가장 강한 포탑은 가장 약한 포탑 선정 기준의 반대
 * 		ㄱ. 공격력이 가장 높은 포탑
 * 		ㄴ. 공격한지 가장 오래된 포탑
 * 		ㄷ. 행과 열의 합이 가장 작은 포탑
 * 		ㄹ. 열 값이 가장 작은 포탑
 * 
 * 
 * 공격 할 때에는 레이저 공격을 먼저 시도하고, 만약 그게 안된다면 포탄 공격을 함.
 * 
 * 2-1) 레이저 공격
 * - 레이저는 다음의 규칙으로 움직임
 * 		ㄱ. 상하좌우의 4개 방향으로 움직일 수 있ㅇ므
 * 		ㄴ. 부서진 포탑이 있는 위치는 지날 수 없음
 * 		ㄷ. 가장자리에서 막힌 방향으로 진행하고자 하면 반대편으로 나옴.
 * 
 * - 레이저 공격은 공격자의 위치에서 공격 대상 포탑까지 최단 경로로 공격(BFS)
 * - 만약 그러한 경로하 존재하지 않으면 포탄 공격 진행
 * - 만약 똑같은 최단 경로가 2개 이상이라면, 우/하/좌/상의 우선순위대로 먼저 움직인 경로 선택
 * - 최단 경로가 정해졌으면, 공격 대상에는 공격자의 공격력 만큼 피해를 입히며, 피해를 입은 포탑은 해당 수치만큼 공격력이 줄어든다
 * - 공격 대상을 제외한 레이저 경로에 있는 포탑도 공격을 받게 되는데, 이 포탑은 공격자 공격력의 절반 만큼 공격을 받는다. (2로 나눈 몫)
 * 		=> 최단 경로 저장해야함
 * 
 * 2-2) 포탄 공격
 * - 공격 대상에 포탄을 던짐
 * - 공격 대상은 공격자 공격력 만큼의 피해를 받음
 * - 주위 8개 방향에 있는 포탑도 피해를 입는데, 공격력의 절반만큼 피해를 받음.
 * - 공격자는 해당 공격에 영향을 받지 않음
 * - 가장자리에 떨어져도 위처럼 다 벽통과
 * 
 * 3. 포탑 부서짐
 * - 공격력이 0 이하가 된 포탑은 부서짐
 * 
 * 4. 포탑 정비
 * - 공격이 끝났으면, 부서지지 않은 포탑 중 공격과 무관했던 포탑은 공격력이 1씩 올라감.
 * - 공격과 무관하다는 뜻: 공격자도 아니며,피해를 ㅣㅇㅂ은 포탑도 아님
 * 
 * 
 * K번 과정이 종료된 후 남아있는 포탑 중 가장 강한 포탑의 공격력은?
 *
 * 문제 해결을 위한 고민)
 * 
 * 1. 공격자, 피공격자 선정
 * => 공격자 포탑, 피공격자 포탑을 각각 Comparable로 만들어 정렬하면 될듯?
 * 
 * 2. 최단 경로를 어떻게 (마킹)저장할 것인가?
 * => 이동하는 경로 다 저장하는게 나으려나..
 * => 다 저장하되 같은 움직임 수면 거기서 return 하는 식으로
 * 
 * 로직)
 * 
 * 1. 약한포탑, 강한 포탑 선정로직
 * 포탑 객체 다 넣어놓고 정렬 한번에 끝내기로
 * 
 * 2. 최단 경로 마킹
 * 새로운 맵 만들어서 거기까지 도달하기 위한 최소턴 계속 저장해가면서 ㄱㄱ
 * 
 * 
 * @author GODJUHYEOK
 *
 */
public class Main {
	
	private static List<Cannon> cannonList; 
	private static final int[] RIGHT = {0, 1}, DOWN = {1, 0}, LEFT = {0, -1}, UP = {-1, 0};
	private static final int[][] DELTA = {RIGHT, DOWN, LEFT, UP};
	private static int[][] deltas = {RIGHT, DOWN, LEFT, UP, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
	private static int N, M, K, turn, cannonCount;
	private static int[][] map, dp;
	
	private static class Node {
		int r;
		int c;
		
		public Node(int r, int c) {
			this.r = r;
			this.c = c;
		}
		
	}
	
	
	private static class Cannon implements Comparable<Cannon> {
		
		
		int id;
		int r;
		int c;
		int attack;
		int lastestAttackTurn = 0;
		boolean alive = true;
		
		public Cannon(int id, int r, int c, int attack) {
			super();
			this.id = id;
			this.r = r;
			this.c = c;
			this.attack = attack;
		}
		
		public int compareTo(Cannon o) {
			if(this.attack == o.attack) {
				if(this.lastestAttackTurn == o.lastestAttackTurn) {
					if((o.r + o.c) == (this.r + this.c)) return this.c - o.c;
					return (o.r + o.c) - (this.r + this.c);
				}
				return o.lastestAttackTurn - this.lastestAttackTurn;
			}
			return this.attack - o.attack;
		}
	}
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());
		
		map = new int[N][M];
		cannonList = new ArrayList<Cannon>();
		for(int i=0, id = 0; i<N; i++) {
			st = new StringTokenizer(br.readLine());
			for(int j=0; j<M; j++) {
				map[i][j] = Integer.parseInt(st.nextToken());
				if(map[i][j] != 0) {
					cannonList.add(new Cannon(id++, i, j, map[i][j]));
					cannonCount++;
				}
			}
		}
		
		for(int t = 1; t <= K; t++) {
			
//			* - 만약 부서지지 않은 포탑이 1개가 된다면 그 중시 턴 중지
//			 * 
//			 * 1. 공격자 선정
//			 * - 공격자로 선정되면 가장 약한 포탑이므로, 핸디캡 적용되어 N+M 만큼 공격력 증가
			if(cannonCount == 1) break;
			
			Collections.sort(cannonList);
			Cannon weakCannon = null;
			
			for(Cannon cannon: cannonList) {
				if(cannon.attack != 0) {
					weakCannon = cannon;
					break;
				}
			}
					
					
			weakCannon.lastestAttackTurn = t;
			map[weakCannon.r][weakCannon.c] += N+M;
			
//			 * 2. 공격자의 공격
//			 * - 선정된 공격자는 **자신을 제외**한 **가장 강한 포탑**을 공격
			Collections.sort(cannonList, Collections.reverseOrder());
			Cannon strongCannon = cannonList.get(0);
			
//			 * 2-1) 레이저 공격
//			 * - 레이저는 다음의 규칙으로 움직임
//			 * 		ㄱ. 상하좌우의 4개 방향으로 움직일 수 있ㅇ므
//			 * 		ㄴ. 부서진 포탑이 있는 위치는 지날 수 없음
//			 * 		ㄷ. 가장자리에서 막힌 방향으로 진행하고자 하면 반대편으로 나옴.
//			 * 
//			 * - 레이저 공격은 공격자의 위치에서 공격 대상 포탑까지 최단 경로로 공격(DFS)
//			 * - 만약 그러한 경로하 존재하지 않으면 포탄 공격 진행
//			 * - 만약 똑같은 최단 경로가 2개 이상이라면, 우/하/좌/상의 우선순위대로 먼저 움직인 경로 선택
//			 * - 최단 경로가 정해졌으면, 공격 대상에는 공격자의 공격력 만큼 피해를 입히며, 피해를 입은 포탑은 해당 수치만큼 공격력이 줄어든다
//			 * - 공격 대상을 제외한 레이저 경로에 있는 포탑도 공격을 받게 되는데, 이 포탑은 공격자 공격력의 절반 만큼 공격을 받는다. (2로 나눈 몫)
//			 * 		=> 최단 경로 저장해야함
			
			Node[][] nodeMap = new Node[N][M];
			boolean[][] visited = new boolean[N][M];
			Queue<int[]> q = new ArrayDeque<int[]>();
			
			q.offer(new int[] {weakCannon.r, weakCannon.c});
			visited[weakCannon.r][weakCannon.c] = true;
			nodeMap[weakCannon.r][weakCannon.c] = new Node(-1, -1);
			
			while(!q.isEmpty()) {

				int[] temp = q.poll();
				if(temp[0] == strongCannon.r && temp[1] == strongCannon.c) break; 
				
				for(int [] d: DELTA) {
					int nr = (temp[0] + d[0] + N) % N;
					int nc = (temp[1] + d[1] + M) % M;
					if(visited[nr][nc] || map[nr][nc] == 0 ) continue;
					
					visited[nr][nc] = true;
					nodeMap[nr][nc] = new Node(temp[0], temp[1]);
					q.offer(new int[] {nr, nc});
				}
			}
			if(visited[strongCannon.r][strongCannon.c]) {
//				 * - 최단 경로가 정해졌으면, 공격 대상에는 공격자의 공격력 만큼 피해를 입히며, 피해를 입은 포탑은 해당 수치만큼 공격력이 줄어든다
//				 * - 공격 대상을 제외한 레이저 경로에 있는 포탑도 공격을 받게 되는데, 이 포탑은 공격자 공격력의 절반 만큼 공격을 받는다. (2로 나눈 몫)
//				 * 		=> 최단 경로 저장해야함
				map[strongCannon.r][strongCannon.c] -= map[weakCannon.r][weakCannon.c];
				// 역추적 시작
				Node temp = nodeMap[strongCannon.r][strongCannon.c];
				while(true) {
					if(temp.r == weakCannon.r && temp.c == weakCannon.c) break;
					map[temp.r][temp.c] -= (map[weakCannon.r][weakCannon.c]) / 2;
					temp = nodeMap[temp.r][temp.c];
				}
				
			} else {
//			 * 2-2) 포탄 공격
//			 * - 공격 대상에 포탄을 던짐
//			 * - 공격 대상은 공격자 공격력 만큼의 피해를 받음
//			 * - 주위 8개 방향에 있는 포탑도 피해를 입는데, 공격력의 절반만큼 피해를 받음.
//			 * - 공격자는 해당 공격에 영향을 받지 않음
//			 * - 가장자리에 떨어져도 위처럼 다 벽통과
				strongCannon.attack -= weakCannon.attack;
				map[strongCannon.r][strongCannon.c] -= map[weakCannon.r][weakCannon.c];
				
				for(int [] d : deltas) {
					int nr = (strongCannon.r + d[0] + N) % N;
					int nc = (strongCannon.c + d[1] + M) % M;
					if(map[nr][nc] == 0 || (nr == weakCannon.r && nc == weakCannon.c)) continue;
					map[nr][nc] -= map[weakCannon.r][weakCannon.c] / 2;
				}
				
			}
			
//			 * 
//			 * 3. 포탑 부서짐
//			 * - 공격력이 0 이하가 된 포탑은 부서짐
			for(Cannon cannon : cannonList) {
				
				if(map[cannon.r][cannon.c] == cannon.attack) cannon.attack++;
				else cannon.attack = map[cannon.r][cannon.c];
				
				if(cannon.attack == 0 && cannon.alive) {
					cannon.alive = false;
					cannonCount--;
				}
			}
			
//			 * 4. 포탑 정비
//			 * - 공격이 끝났으면, 부서지지 않은 포탑 중 공격과 무관했던 포탑은 공격력이 1씩 올라감.
//			 * - 공격과 무관하다는 뜻: 공격자도 아니며,피해를 ㅣㅇㅂ은 포탑도 아님
//			 * 
//			 * 
//			 * K번 과정이 종료된 후 남아있는 포탑 중 가장 강한 포탑의 공격력은?
			
		}
		
		Collections.sort(cannonList, Collections.reverseOrder());
		
		for(Cannon cannon : cannonList) {
			if(cannon.alive) {
				System.out.println(cannon.attack);
				return;
			}
		}
		
		
		
		
	}

}