public class Quora {

    static final int NULL = -1, OPEN = 0, BLOCKED = 1, START = 2, END = 3,
                    XSHIFT = 3, YMASK = (1<<XSHIFT)-1,  //SHIFT needs to be increased if X or Y need more than SHIFT bits
                    D[] = new int[]{1, -1, -(1<<XSHIFT), 1<<XSHIFT}, //deltas for Left, Right, Up, Down
                    CACHE_SIZE = 1<<20;

    static final boolean VISITABLE = true;

    static boolean matrix[];

    static int remaining,
               start, end,     // last SHIFT bits are y; x is stored in SHIFT bits on the left of y.
               X, Y;

    static long state; // use 64-bits to represent upto 64 cells, always make sure you use 1L when shifting, for large cases use BitSet

    static java.util.HashMap<Long, Integer> cache = new java.util.HashMap<Long, Integer>(CACHE_SIZE);

    static int compute(final int p) {
        matrix[p] = !VISITABLE;
        remaining--;
        final int stateBit = ((p>>XSHIFT)-1)*Y + ((p&YMASK)-1);
        state |= (1L<<stateBit);

        try {
            Integer val = cache.get(state);
            if(val != null)
                return val;

            int ans = 0;

            if(numOfExits(end) == 0 || !isFloodFillable()) {
                cache.put(state, ans = remaining == 1 ? 1 : 0);
                return ans;
            }

            int onlyChoice = NULL;

            for(int d : D) {
                if(matrix[d += p] && d != end && numOfExits(d) == 1) {
                    if(onlyChoice != NULL) {
                        cache.put(state, ans);
                        return ans;
                    }
                    onlyChoice = d;
                }
            }

            if(onlyChoice == NULL) {
                for(int d : D)
                    if(matrix[d += p])
                        ans += compute(d);
                //cache.put(p, ans); //TODO: State should be visited+position and not just visited.
            } else
                cache.put(state, ans = compute(onlyChoice));

            return ans;
        } finally {
            matrix[p] = VISITABLE;
            state &= ~(1L<<stateBit);
            remaining++;
        }
    }

    static int queue[];
    static boolean isFloodFillable() {
        int p = end, tail = 0;
        queue[tail++] = p;
        matrix[p] = !VISITABLE;

        for(int head = 0; tail > head; head++) {
            p = queue[head];
            for(int d : D)
                if(matrix[d += p])
                    matrix[queue[tail++] = d] = !VISITABLE;
        }

        for(int i = 0; i < tail; i++)
            matrix[queue[i]] = VISITABLE;

        return remaining == tail;
    }

    static int numOfExits(int p) {
        int exits = 0;
        for(int d : D)
            if(matrix[p + d])
                exits++;
        return exits;
    }

    public static void main(String args[]) {
        java.util.Scanner in = new java.util.Scanner(System.in);
        remaining = (Y = in.nextInt())*(X = in.nextInt());
        matrix = new boolean[((X+2)<<XSHIFT) | (Y+2)];
        int p;
        for(int i = 1; i <= X; i++)
            for(int j = 1; j <= Y; j++) {
                matrix[p = (i<<XSHIFT) | j] = VISITABLE;
                switch(in.nextInt()) {
                    case START: start = p; break;
                    case END: end = p; break;
                    case BLOCKED: {
                        remaining--;
                        matrix[p] = !VISITABLE;
                        state |= 1L<<((i-1)*Y + (j-1));
                    }
                }
            }
        queue = new int[remaining];
        System.out.println(compute(start));
    }
}
