/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8233033 8235984
 * @summary Tests if partially peeled statements are not executed before the loop predicates by bailing out of loop unswitching.
 *
 * @run main/othervm -Xbatch
 *      -XX:CompileCommand=compileonly,compiler.loopopts.PartialPeelingUnswitch::test*
 *      -XX:CompileCommand=dontinline,compiler.loopopts.PartialPeelingUnswitch::dontInline
 *      compiler.loopopts.PartialPeelingUnswitch
 */

package compiler.loopopts;

public class PartialPeelingUnswitch {

    public static int iFld;
    public static int x = 42;
    public static int y = 31;
    public static int z = 22;
    public static int[] iArr = new int[10];

    public int test() {
        /*
         * The inner loop of this test is first partially peeled and then unswitched. An uncommon trap is hit in one
         * of the cloned loop predicates for the fast loop (set up at unswitching stage). The only partially peeled
         * statement "iFld += 7" was wrongly executed before the predicates (and before the loop itself).
         * When hitting the uncommon trap, "iFld >>= 1" was not yet executed. As a result, the interpreter directly
         * reexecuted "iFld += 7" again. This resulted in a wrong result for "iFld". The fix in 8233033 makes peeled
         * statements control dependant on the cloned loop predicates such that they are executed after them. However,
         * some cases are not handled properly. For now, the new fix in 8235984 just bails out of loop unswitching.
         */
        iFld = 13;
        for (int i = 0; i < 8; i++) {
            int j = 10;
            while (--j > 0) {
                iFld += -7;
                switch ((i * 5) + 102) {
                case 120:
                    break;
                case 103:
                    break;
                case 116:
                    break;
                default:
                    iFld >>= 1;
                }
            }
        }
        return iFld;
    }

    public int test2() {
        /*
         * Same nested loop structure as in test() but with more statements that are partially peeled from the inner loop.
         * Afterwards the inner loop is unswitched.
         */
        iFld = 13;
        int k = 0;
        for (int i = 0; i < 8; i++) {
            int j = 10;
            while (--j > 0) {
                // All statements before the switch expression are partially peeled
                iFld += -7;
                x = y + iFld;
                y = iArr[5];
                k = 6;
                iArr[5] = 5;
                iArr[6] += 23;
                iArr[7] = iArr[8] + iArr[6];
                iArr[j] = 34;
                switch ((i * 5) + 102) {
                case 120:
                    break;
                case 103:
                    break;
                case 116:
                    break;
                default:
                    iFld >>= 1;
                }
            }
        }
        return iFld + k;
    }

    public int test3() {
        iFld = 13;
        if (z < 34) {
            z = 34;
        }

        for (int i = 0; i < 8; i++) {
            int j = 10;
            while (--j > 0) {
                iFld += -7;
                iArr[5] = 8;
                x = iArr[6];
                y = x;
                for (int k = 50; k < 51; k++) {
                    x = iArr[7];
                }
                switch ((i * 5) + 102) {
                case 120:
                    return iFld;
                case 103:
                    break;
                case 116:
                    break;
                default:
                    if (iFld == -7) {
                        return iFld;
                    }
                    z = iArr[5];
                    iFld >>= 1;
                }
            }
            iArr[5] = 34;
            dontInline(iArr[5]);
        }
        return iFld;
    }

    public int test4() {
        iFld = 13;
        if (z < 34) {
            z = 34;
        }

        for (int i = 0; i < 8; i++) {
            int j = 10;
            while (--j > 0) {
                iFld += -7;
                iArr[5] = 8;
                x = iArr[6];
                y = x;
                for (int k = 50; k < 51; k++) {
                    x = iArr[7];
                }
                switch ((i * 5) + 102) {
                case 120:
                    return iFld;
                case 103:
                    break;
                case 116:
                    break;
                default:
                    if (iFld == -7) {
                        return iFld;
                    }
                    z = iArr[5];
                    iFld >>= 1;
                }
            }
            iArr[5] = 34;
        }
        return iFld;
    }

    public int test5() {
        iFld = 13;
        for (int i = 0; i < 8; i++) {
            int j = 10;
            while (--j > 0) {
                iFld += -7;
                iArr[5] = 8;
                x = iArr[6];
                y = x;
                for (int k = 50; k < 51; k++) {
                    x = iArr[7];
                }
                switch ((i * 5) + 102) {
                case 120:
                    return iFld;
                case 103:
                    break;
                case 116:
                    break;
                default:
                    iFld >>= 1;
                }
            }
        }
        return iFld;
    }

    public int test6() {
        iFld = 13;
        for (int i = 0; i < 8; i++) {
            int j = 10;
            while (--j > 0) {
                iFld += -7;
                iArr[5] = 8;
                x = iArr[6];
                y = x;
                switch ((i * 5) + 102) {
                case 120:
                    return iFld;
                case 103:
                    break;
                case 116:
                    break;
                default:
                    iFld >>= 1;
                }
            }
        }
        return iFld;
    }

    public int test7() {
        iFld = 13;
        for (int i = 0; i < 8; i++) {
            int j = 10;
            while (--j > 0) {
                iFld += -7;
                iArr[5] = 8;
                switch ((i * 5) + 102) {
                case 120:
                    return iFld;
                case 103:
                    break;
                case 116:
                    break;
                default:
                    iFld >>= 1;
                }
            }
        }
        return iFld;
    }

    public static void main(String[] strArr) {
        PartialPeelingUnswitch _instance = new PartialPeelingUnswitch();
        for (int i = 0; i < 2000; i++) {
            int result = _instance.test();
            if (result != -7) {
                throw new RuntimeException("Result should always be -7 but was " + result);
            }
        }

        for (int i = 0; i < 2000; i++) {
            int result = _instance.test2();
            check(-1, result);
            check(-7, iFld);
            check(-9, x);
            check(5, y);
            check(5, iArr[5]);
            check(149, iArr[6]);
            check(183, iArr[7]);

            // Reset fields
            for (int j = 0; j < 10; j++) {
                iArr[j] = 0;
            }
            x = 42;
            y = 31;
        }

        for (int i = 0; i < 2000; i++) {
            _instance.test3();
            _instance.test4();
            _instance.test5();
            _instance.test6();
            _instance.test7();
        }

        for (int i = 0; i < 2000; i++) {
            if (i % 2 == 0) {
                z = 23;
            }
            _instance.test3();
            _instance.test4();
        }
    }

    public static void check(int expected, int actual) {
        if (expected != actual) {
            throw new RuntimeException("Wrong result, expected: " + expected + ", actual: " + actual);
        }
    }

    public void dontInline(int i) { }
}
