import java.util.Comparator;
import java.util.Scanner;

/**
 *
 * @author Arun Kumar
 */
public class GenerateMagicSquares {
    
    int order;
    int max;
    int magic_constant;
  
    boolean print_squares = true;
    Comparator<int[]> int_arr_comparator;
    
      public GenerateMagicSquares(int order) {
        this.order = order;
        this.max = this.order * this.order;
        this.magic_constant = (this.order * this.order * this.order + this.order) / 2;
        this.int_arr_comparator = new Comparator<int[]>() {
            public int compare(int[] arr1, int[] arr2) {
                for (int i = 0; i < arr1.length; i++) {
                    if (arr1[i] != arr2[i]) {
                        return (arr1[i] > arr2[i] ? 1 : -1);
                    }
                }
                return 0;
            }
        };
    }
   
      public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        System.out.print("What order [3,4] Magic Square would you like to solve? ");
        int order = console.nextInt();

        if(order != 3 && order != 4){
            System.out.println("please enter order 3 or 4");
            return;
        }
        GenerateMagicSquares obj = new GenerateMagicSquares(order);

        System.out.println("Finding all magic matricies of order " + order);

        obj.init_magic_tree();

        System.out.println("Sum in each row & each column =  " + order + "*(" + order + "^2+1)/2 = " + obj.magic_constant);

    }

    static String str_repeat(String str, int repeat) {
        String result = "";
        for (int i = 0; i < repeat; i++) {
            result += str;
        }
        return result;
    }

    public void init_magic_tree() {
        
        Permutations sum_permutations_list = new Permutations(
                this);        
        MagicSquares magic_tree = new MagicSquares(this, sum_permutations_list);
        magic_tree.build_tree();
        
    }
}
