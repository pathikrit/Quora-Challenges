import java.util.HashMap;

public class Quora {

	static final int NULL = -1, OPEN = 0, BLOCKED = 1, START = 2, END = 3,
			XSHIFT = 3, YMASK = (1 << XSHIFT) - 1,  //SHIFT needs to be increased if X or Y need more than SHIFT bits
			D[] = new int[]{1, -1, -(1 << XSHIFT), 1 << XSHIFT}, //deltas for Left, Right, Up, Down
			STATE_CACHE_SIZE = 1 << 20, POSITION_CACHE_SIZE = 1 << 4;

	static final boolean VISITABLE = true;

	static boolean matrix[];

	static int remaining,
			start, end,     // last SHIFT bits are y; x is stored in SHIFT bits on the left of y.
			X, Y;

	static long state; // use 64-bits to represent upto 64 cells, always make sure you use 1L when shifting, for large cases use BitSet

	static HashMap<Long, HashMap<Integer, Integer>> cache = new HashMap<Long, HashMap<Integer, Integer>>(STATE_CACHE_SIZE);

	static int compute(final int p) {
		matrix[p] = !VISITABLE;
		remaining--;
		final int stateBit = ((p >> XSHIFT) - 1) * Y + ((p & YMASK) - 1);
		state |= (1L << stateBit);

		try {
			{
				HashMap<Integer, Integer> positionCache = cache.get(state);
				Integer cachedValue = positionCache == null ? null : positionCache.get(p);
				if (cachedValue != null)
					return cachedValue;
			}

			if (numOfExits(end) == 0 || !isFloodFillable())
				return cacheAndReturn(state, p, remaining == 1 ? 1 : 0);

			int onlyChoice = NULL;

			for (int d : D) {
				if (matrix[d += p] && d != end && numOfExits(d) == 1) {
					if (onlyChoice != NULL)
						return cacheAndReturn(state, p, 0);
					onlyChoice = d;
				}
			}

			if (onlyChoice == NULL) {
				int paths = 0;
				for (int d : D)
					if (matrix[d += p])
						paths += compute(d);
				return cacheAndReturn(state, p, paths);
			}

			return cacheAndReturn(state, p, compute(onlyChoice));

		} finally {
			matrix[p] = VISITABLE;
			state &= ~(1L << stateBit);
			remaining++;
		}
	}

	static int cacheAndReturn(final long state, final int p, final int ans) {
		HashMap<Integer, Integer> positionCache = cache.get(state);
		if (positionCache == null)
			cache.put(state, positionCache = new HashMap<Integer, Integer>(POSITION_CACHE_SIZE));
		positionCache.put(p, ans);
		return ans;
	}

	static int queue[];

	static boolean isFloodFillable() {
		int p = end, tail = 0;
		queue[tail++] = p;
		matrix[p] = !VISITABLE;

		for (int head = 0; tail > head; head++) {
			p = queue[head];
			for (int d : D)
				if (matrix[d += p])
					matrix[queue[tail++] = d] = !VISITABLE;
		}

		for (int i = 0; i < tail; i++)
			matrix[queue[i]] = VISITABLE;

		return remaining == tail;
	}

	static int numOfExits(int p) {
		int exits = 0;
		for (int d : D)
			if (matrix[p + d])
				exits++;
		return exits;
	}

	public static void main(String args[]) {
		java.util.Scanner in = new java.util.Scanner(System.in);
		remaining = (Y = in.nextInt()) * (X = in.nextInt());
		matrix = new boolean[((X + 2) << XSHIFT) | (Y + 2)];
		int p;
		for (int i = 1; i <= X; i++)
			for (int j = 1; j <= Y; j++) {
				matrix[p = (i << XSHIFT) | j] = VISITABLE;
				switch (in.nextInt()) {
					case START:
						start = p;
						break;
					case END:
						end = p;
						break;
					case BLOCKED: {
						remaining--;
						matrix[p] = !VISITABLE;
						state |= 1L << ((i - 1) * Y + (j - 1));
					}
				}
			}
		queue = new int[remaining];
		System.out.println(compute(start));
	}
}
