import java.util.*;
public class GoFish {
    private final String[] SUITS = { "C", "D", "H", "S" };
    private final String[] RANKS = { "A", "2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K" };

    private char whoseTurn;
    private final Player player;
    private final Player computer;
    private List<Card> deck;
    private final Scanner in;
    private final boolean multiplayer;
    private ArrayList<Card> cpuOppHandTook = new ArrayList<>();
    private ArrayList<Card> cpuOppHandDoesNotHave = new ArrayList<>();
    public GoFish(boolean multi) {
        this.whoseTurn = 'P';
        this.player = new Player();
        this.computer = new Player();
        this.in = new Scanner(System.in);
        this.multiplayer = multi;
    }

    public void play() {
        shuffleAndDeal();

        // play the game until someone wins

        while (true) {
            if (whoseTurn == 'P') {
                whoseTurn = takeTurn(false);

                if (player.findAndRemoveBooks()) {
                    System.out.println("PLAYER: Oh, that's a book!");
                    showBooks(false);
                }
            } else if (whoseTurn == 'C') {
                whoseTurn = takeTurn(true);

                if (computer.findAndRemoveBooks()) {
                    if(multiplayer){
                        System.out.println("PLAYER 2: Oh, that's a book!");
                    }else{
                        System.out.println("CPU: Oh, that's a book!");
                    }
                    showBooks(true);
                }
            }

            // the games doesn't end until all 13 books are completed. the player with the
            // most books at the end of the game wins.

            if (player.getBooks().size() + computer.getBooks().size() == 13) {
                if (player.getBooks().size() > computer.getBooks().size()) {
                    System.out.println("\nCongratulations, PLAYER wins!");
                } else if (multiplayer && computer.getBooks().size() > player.getBooks().size()){
                    System.out.println("\nCongratulations, PLAYER 2 wins!");
                } else {
                    System.out.println("\nOh, better luck next time. This game goes to the computer...");
                }
                break;
            }
        }
    }

    public void shuffleAndDeal() {
        if (deck == null) {
            initializeDeck();
        }
        Collections.shuffle(deck);  // shuffles the deck

        while (player.getHand().size() < 7) {
            player.takeCard(deck.remove(0));    // deal 7 cards to the
            computer.takeCard(deck.remove(0));  // player and the computer
        }
    }

    ////////// PRIVATE METHODS /////////////////////////////////////////////////////

    private void initializeDeck() {
        deck = new ArrayList<>(52);

        for (String suit : SUITS) {
            for (String rank : RANKS) {
                deck.add(new Card(rank, suit));     // adds 52 cards to the deck (13 ranks, 4 suits)
            }
        }
    }

    private char takeTurn(boolean cpu) {
        showHand(cpu);
        showBooks(cpu);

        // if requestCard returns null, then the hand was empty and new card was drawn.
        // this restarts the turn, ensuring the updated hand is printed to the console.

        Card card = requestCard(cpu);
        if (card == null) {
            return cpu ? 'C' : 'P';     // restart this turn with updated hand
        }

        // check if your opponent has the card you requested. it will be automatically
        // relinquished if you do. otherwise, draw from the deck. return the character
        // code for whose turn it should be next.

        if (!cpu) {
            if (computer.hasCard(card)) {
                if(multiplayer){
                    System.out.println("PLAYER 2: Yup, here you go!");
                }else{
                    System.out.println("CPU: Yup, here you go!");
                    cpuOppHandTook.add(card);
                }
                computer.relinquishCard(player, card);

                return 'P';
            } else {
                if(multiplayer){
                    System.out.println("PLAYER 2: Nope, go fish!");
                }else{
                    System.out.println("CPU: Nope, go fish!");
                }

                player.takeCard(deck.remove(0));

                return 'C';
            }
        } else {
            if (player.hasCard(card)) {
                if(multiplayer) {
                    System.out.println("PLAYER: Yup, here you go!");
                } else{
                    System.out.println("CPU: Oh, you do? Well, hand it over!");
                }
                player.relinquishCard(computer, card);

                return 'C';
            } else {
                if(multiplayer) {
                    System.out.println("PLAYER 2: Ah, I guess I'll go fish...");
                } else{
                    System.out.println("CPU: Ah, I guess I'll go fish...");
                    cpuOppHandDoesNotHave.add(card);
                }
                computer.takeCard(deck.remove(0));

                return 'P';
            }
        }
    }

    private Card requestCard(boolean cpu) {
        Card card = null;

        // request a card from your opponent, ensuring that the request is valid.
        // if your hand is empty, we return null to signal the calling method to
        // restart the turn. otherwise, we return the requested card.
        while (card == null) {
            if (!cpu) {
                if (player.getHand().size() == 0) {
                    player.takeCard(deck.remove(0));
                    return null;
                } else {
                    System.out.print("PLAYER: Got any... ");
                    String rank = in.nextLine().trim().toUpperCase();
                    checkQuit(rank);
                    card = Card.getCardByRank(rank);
                }
            } else if (multiplayer){
                System.out.print("PLAYER 2: Got any... ");
                String rank = in.nextLine().trim().toUpperCase();
                checkQuit(rank);
                card = Card.getCardByRank(rank);
            }else {
                if (computer.getHand().size() == 0) {
                    computer.takeCard(deck.remove(0));

                    return null;
                } else {
                    card = computer.getCardByNeed(cpuOppHandDoesNotHave, cpuOppHandTook);
                    System.out.println("CPU: Got any... " + card.getRank());
                }
            }
        }

        return card;
    }

    private void showHand(boolean cpu) {
        if (!cpu) {
            System.out.println("\nPLAYER hand: " + player.getHand());   // only show player's hand
        }else if(cpu && multiplayer){
            System.out.println("\nPLAYER 2 hand: " + computer.getHand());
        }
    }

    private void showBooks(boolean cpu) {
        if (!cpu) {
            System.out.println("PLAYER books: " + player.getBooks());   // shows the player's books
        } else if (multiplayer) {
            System.out.println("PLAYER 2 books: " + computer.getBooks());
        }else {
            System.out.println("\nCPU books: " + computer.getBooks());  // shows the computer's books
        }
    }

    ////////// MAIN METHOD /////////////////////////////////////////////////////////

    public static void main(String[] args) {
        System.out.println("#########################################################");
        System.out.println("#                                                       #");
        System.out.println("#   ####### #######   ####### ####### ####### #     #   #");
        System.out.println("#   #       #     #   #          #    #       #     #   #");
        System.out.println("#   #  #### #     #   #####      #    ####### #######   #");
        System.out.println("#   #     # #     #   #          #          # #     #   #");
        System.out.println("#   ####### #######   #       ####### ####### #     #   #");
        System.out.println("#                                                       #");
        System.out.println("#   A human v. CPU rendition of the classic card game   #");
        System.out.println("#   Go Fish. Play the game, read and modify the code,   #");
        System.out.println("#   and make it your own!                               #");
        System.out.println("#                                                       #");
        System.out.println("#########################################################");

        Scanner init = new Scanner(System.in);
        boolean multi = startGame(init);
        new GoFish(multi).play();
    }
    private static boolean startGame(Scanner init){
        String option;
        while(true){
            System.out.print("Enter \"M\" for a local multiplayer game or \"C\" for a game against CPU (Enter \"quit\" at any time to quit): ");
            option = init.nextLine().toUpperCase();
            checkQuit(option);
            if(option.equals("M")){
                return true;
            } else if (option.equals("C")){
                return false;
            }
        }
    }
    private static void checkQuit (String s){
        if(s.toUpperCase().equals("QUIT")){
            System.exit(0);
        }
    }
}
