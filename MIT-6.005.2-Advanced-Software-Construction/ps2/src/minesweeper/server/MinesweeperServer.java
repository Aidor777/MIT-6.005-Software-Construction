/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import minesweeper.Board;
import minesweeper.Position;

/**
 * Multiplayer Minesweeper server.
 */
public class MinesweeperServer {

    // System thread safety argument
    // There is a unique board per server, not visible by other threads.
    // Each client is running inside its own thread, with the minimum amount of shared mutable data as possible.
    // The board is a thread-safe class, as all calls to public methods are synchronized.
    // In the very special case of digging, when it is required to perform a check before the actual digging,
    // the lock of the board is acquired so that the state of the board cannot change in between.
    // The counter of clients, although mutable, is a thread-safe AtomicInteger, guaranteeing correctness.
    // References to internal components of the board that could be mutated (the squares) are never shared.
    // Regarding liveness, the board is a singleton, which will thus never try to acquire the lock to another board.

    /**
     * Default server port.
     */
    private static final int DEFAULT_PORT = 4444;
    /**
     * Maximum port number as defined by ServerSocket.
     */
    private static final int MAXIMUM_PORT = 65535;

    /**
     * Default square board size.
     */
    private static final int DEFAULT_SIZE = 10;

    private static final String BYE_MESSAGE = "Bye";

    private static final String BOOM_MESSAGE = "BOOM!";

    /**
     * Socket for receiving incoming connections.
     */
    private final ServerSocket serverSocket;

    /**
     * True if the server should *not* disconnect a client after a BOOM message.
     */
    private final boolean debug;

    /**
     * The Minesweeper board (threadsafe)
     */
    private final Board board;

    private final AtomicInteger connectedClients = new AtomicInteger(0);

    // Abstraction function
    // Represents the server-side of a multiplayer Minesweeper game. It holds the board of the game
    // and the amount of connected clients, and a server socket to listen to client connections

    // Rep invariant
    // The server socket is noy null, the board is not null, and the number of connected clients is >= 0

    // Rep exposure
    // All fields are private, the only public method is main()

    /**
     * Make a MinesweeperServer that listens for connections on port.
     *
     * @param port  port number, requires 0 <= port <= 65535
     * @param debug debug mode flag
     * @param board the board to play with
     * @throws IOException if an error occurs opening the server socket
     */
    private MinesweeperServer(int port, boolean debug, Board board) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.debug = debug;
        this.board = board;
        checkRep();
    }

    private void checkRep() {
        assert serverSocket != null;
        assert board != null;
        assert connectedClients != null;
        assert connectedClients.get() >= 0;
    }

    /**
     * Run the server, listening for client connections and handling them.
     * Never returns unless an exception is thrown.
     *
     * @throws IOException if the main server socket is broken
     *                     (IOExceptions from individual clients do *not* terminate serve())
     */
    private void serve() throws IOException {
        while (true) {
            // block until a client connects (force evaluation before passing it in the lambda)
            Socket socket = this.serverSocket.accept();
            this.connectedClients.incrementAndGet();
            checkRep();

            // handle the client in its own dedicated thread
            new Thread(() -> {
                try {
                    handleConnection(socket);
                } catch (IOException e) {
                    //e.printStackTrace(); // but Do not terminate serve()
                } finally {
                    // each thread must be responsible for closing their connection
                    try {
                        this.connectedClients.decrementAndGet();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace(); // not much we can do if that happens...
                    }
                    checkRep();
                }
            }).start();
        }
    }

    /**
     * Handle a single client connection. Returns when client disconnects.
     *
     * @param socket socket where the client is connected
     * @throws IOException if the connection encounters an error or terminates unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(helloMessage());

            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);

                if (BYE_MESSAGE.equals(output)) {
                    socket.close();
                }

                out.println(output);

                if (!this.debug && BOOM_MESSAGE.equals(output)) {
                    socket.close();
                }
            }
        }
    }

    /**
     * Handler for client input, performing requested operations and returning an output message.
     *
     * @param input message from client
     * @return message to client, or the help message if the message was not understood
     */
    private String handleRequest(String input) {
        String regex = "(look)|(help)|(bye)|"
                + "(dig -?\\d+ -?\\d+)|(flag -?\\d+ -?\\d+)|(deflag -?\\d+ -?\\d+)";
        if (!input.matches(regex)) {
            // invalid input, return a message explaining valid inputs
            return helpMessage();
        }
        String[] tokens = input.split(" ");
        switch (tokens[0]) {
            case "look":
                // 'look' request
                return this.board.toString();
            case "help":
                // 'help' request
                return helpMessage();
            case "bye":
                // 'bye' request
                return BYE_MESSAGE;
            case "dig":
                int x = Integer.parseInt(tokens[1]);
                int y = Integer.parseInt(tokens[2]);

                // 'dig x y' request
                if (positionWithinBoard(x, y)) {
                    Position position = Position.of(x, y);

                    synchronized (this.board) {
                        boolean hasBomb = board.positionContainsBomb(position);
                        board.digAtPosition(position, hasBomb);

                        if (hasBomb) {
                            return BOOM_MESSAGE;
                        }
                    }
                }
                return this.board.toString();
            case "flag":
                x = Integer.parseInt(tokens[1]);
                y = Integer.parseInt(tokens[2]);

                // 'flag x y' request
                if (positionWithinBoard(x, y)) {
                    this.board.flagPosition(Position.of(x, y));
                }
                return this.board.toString();
            case "deflag":
                x = Integer.parseInt(tokens[1]);
                y = Integer.parseInt(tokens[2]);

                // 'deflag x y' request
                if (positionWithinBoard(x, y)) {
                    this.board.deflagPosition(Position.of(x, y));
                }
                return this.board.toString();
            default:
                throw new UnsupportedOperationException("Should not happen");
        }
    }

    /**
     * @return a welcome message for a client that just connected
     */
    private String helloMessage() {
        return "Welcome to Minesweeper. Players: " + this.connectedClients.get() + " including you. " +
                "Board: " + board.getWidth() + " columns by " + board.getHeight() + " rows." +
                "Type 'help' for help.";
    }

    /**
     * @return a message explaining how to play
     */
    private String helpMessage() {
        return "Send a message to perform an action. Possible messages: \"help\" (get this message again) / " +
                "\"dig X Y\" (dig at position (X, Y) / \"flag X Y\" (flag position (X, Y)) / " +
                "\"deflag X Y\" (remove the flag at position (X, Y)) / \"look\" (have a look at the board) / " +
                "\"bye\" (exit the game)";
    }

    private boolean positionWithinBoard(int x, int y) {
        return x >= 0 && y >= 0 && x < this.board.getWidth() && y < this.board.getHeight();
    }

    /**
     * Start a MinesweeperServer using the given arguments.
     *
     * <br> Usage:
     * MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]
     *
     * <br> The --debug argument means the server should run in debug mode. The server should disconnect a
     * client after a BOOM message if and only if the --debug flag was NOT given.
     * Using --no-debug is the same as using no flag at all.
     * <br> E.g. "MinesweeperServer --debug" starts the server in debug mode.
     *
     * <br> PORT is an optional integer in the range 0 to 65535 inclusive, specifying the port the server
     * should be listening on for incoming connections.
     * <br> E.g. "MinesweeperServer --port 1234" starts the server listening on port 1234.
     *
     * <br> SIZE_X and SIZE_Y are optional positive integer arguments, specifying that a random board of size
     * SIZE_X*SIZE_Y should be generated.
     * <br> E.g. "MinesweeperServer --size 42,58" starts the server initialized with a random board of size
     * 42*58.
     *
     * <br> FILE is an optional argument specifying a file pathname where a board has been stored. If this
     * argument is given, the stored board should be loaded as the starting board.
     * <br> E.g. "MinesweeperServer --file boardfile.txt" starts the server initialized with the board stored
     * in boardfile.txt.
     *
     * <br> The board file format, for use with the "--file" option, is specified by the following grammar:
     * <pre>
     *   FILE ::= BOARD LINE+
     *   BOARD ::= X SPACE Y NEWLINE
     *   LINE ::= (VAL SPACE)* VAL NEWLINE
     *   VAL ::= 0 | 1
     *   X ::= INT
     *   Y ::= INT
     *   SPACE ::= " "
     *   NEWLINE ::= "\n" | "\r" "\n"?
     *   INT ::= [0-9]+
     * </pre>
     *
     * <br> If neither --file nor --size is given, generate a random board of size 10x10.
     *
     * <br> Note that --file and --size may not be specified simultaneously.
     *
     * @param args arguments as described
     */
    public static void main(String[] args) {
        // Command-line argument parsing is provided. Do not change this method.
        boolean debug = false;
        int port = DEFAULT_PORT;
        int sizeX = DEFAULT_SIZE;
        int sizeY = DEFAULT_SIZE;
        Optional<File> file = Optional.empty();

        Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        try {
            while (!arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    switch (flag) {
                        case "--debug":
                            debug = true;
                            break;
                        case "--no-debug":
                            debug = false;
                            break;
                        case "--port":
                            port = Integer.parseInt(arguments.remove());
                            if (port < 0 || port > MAXIMUM_PORT) {
                                throw new IllegalArgumentException("port " + port + " out of range");
                            }
                            break;
                        case "--size":
                            String[] sizes = arguments.remove().split(",");
                            sizeX = Integer.parseInt(sizes[0]);
                            sizeY = Integer.parseInt(sizes[1]);
                            file = Optional.empty();
                            break;
                        case "--file":
                            sizeX = -1;
                            sizeY = -1;
                            file = Optional.of(new File(arguments.remove()));
                            if (!file.get().isFile()) {
                                throw new IllegalArgumentException("file not found: \"" + file.get() + "\"");
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for " + flag);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]");
            return;
        }

        try {
            runMinesweeperServer(debug, file, sizeX, sizeY, port);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Start a MinesweeperServer running on the specified port, with either a random new board or a
     * board loaded from a file.
     *
     * @param debug The server will disconnect a client after a BOOM message if and only if debug is false.
     * @param file  If file.isPresent(), start with a board loaded from the specified file,
     *              according to the input file format defined in the documentation for main(..).
     * @param sizeX If (!file.isPresent()), start with a random board with width sizeX
     *              (and require sizeX > 0).
     * @param sizeY If (!file.isPresent()), start with a random board with height sizeY
     *              (and require sizeY > 0).
     * @param port  The network port on which the server should listen, requires 0 <= port <= 65535.
     * @throws IOException if a network error occurs
     */
    private static void runMinesweeperServer(boolean debug, Optional<File> file, int sizeX, int sizeY, int port) throws IOException {
        Board board = file.isPresent() ? Board.fromFile(file.get()) : Board.fromDimensions(sizeX, sizeY);
        MinesweeperServer server = new MinesweeperServer(port, debug, board);
        server.serve();
    }
}
