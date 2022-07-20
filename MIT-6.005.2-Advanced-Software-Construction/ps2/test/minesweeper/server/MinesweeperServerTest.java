/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper.server;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Test suite for the MinesweeperServer
 */
public class MinesweeperServerTest {

    private static final String LOCALHOST = "127.0.0.1";

    private static final int MAX_CONNECTION_ATTEMPTS = 10;

    private static final String BOARDS_PKG = "minesweeper/boards/";

    private static final Set<Integer> portsUsed = new HashSet<>();

    private static int port;

    @Before
    public void init() {
        port = -1;
        do {
            port = 4000 + new Random().nextInt(1 << 15);
        } while (portsUsed.contains(port));
        portsUsed.add(port);
    }

    /**
     * Start a MinesweeperServer in debug mode with a board file from BOARDS_PKG.
     *
     * @param boardFile board to load
     * @return thread running the server
     * @throws IOException if the board file cannot be found
     */
    private static Thread startMinesweeperServer(String boardFile) throws IOException {
        final URL boardURL = ClassLoader.getSystemClassLoader().getResource(BOARDS_PKG + boardFile);
        if (boardURL == null) {
            throw new IOException("Failed to locate resource " + boardFile);
        }
        final String boardPath;
        try {
            boardPath = new File(boardURL.toURI()).getAbsolutePath();
        } catch (URISyntaxException urise) {
            throw new IOException("Invalid URL " + boardURL, urise);
        }
        final String[] args = new String[]{
                "--debug",
                "--port", Integer.toString(port),
                "--file", boardPath
        };
        Thread serverThread = new Thread(() -> MinesweeperServer.main(args));
        serverThread.start();
        return serverThread;
    }

    /**
     * Connect to a MinesweeperServer and return the connected socket.
     *
     * @param server abort connection attempts if the server thread dies
     * @return socket connected to the server
     * @throws IOException if the connection fails
     */
    private static Socket connectToMinesweeperServer(Thread server) throws IOException {
        int attempts = 0;
        while (true) {
            try {
                Socket socket = new Socket(LOCALHOST, port);
                socket.setSoTimeout(3000);
                return socket;
            } catch (ConnectException ce) {
                if (!server.isAlive()) {
                    throw new IOException("Server thread not running");
                }
                if (++attempts > MAX_CONNECTION_ATTEMPTS) {
                    throw new IOException("Exceeded max connection attempts", ce);
                }
                try {
                    Thread.sleep(attempts * 10L);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    @Test(timeout = 10000)
    public void publishedTest() throws IOException {

        Thread thread = startMinesweeperServer("server-board-published.txt");

        Socket socket = connectToMinesweeperServer(thread);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            assertTrue("Welcome message", in.readLine().startsWith("Welcome"));

            out.println("help");
            assertNotNull(in.readLine());

            out.println("look");
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());

            out.println("dig 3 1");
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - 1 - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());

            out.println("flag 1 1");
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- F - 1 - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());

            out.println("deflag 1 1");
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - 1 - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());
            assertEquals("- - - - - - -", in.readLine());

            out.println("dig 4 1");
            assertEquals("BOOM!", in.readLine());

            out.println("look"); // debug mode is on
            assertEquals("             ", in.readLine());
            assertEquals("             ", in.readLine());
            assertEquals("             ", in.readLine());
            assertEquals("             ", in.readLine());
            assertEquals("             ", in.readLine());
            assertEquals("1 1          ", in.readLine());
            assertEquals("- 1          ", in.readLine());

            out.println("bye");
        }
        socket.close();
        thread.interrupt();
    }

    @Test(timeout = 10000)
    public void testMinesweeperServer_multiClients() throws IOException {
        Thread thread = startMinesweeperServer("server-board-published.txt");

        try (Socket clientOne = connectToMinesweeperServer(thread);
             BufferedReader inOne = new BufferedReader(new InputStreamReader(clientOne.getInputStream()));
             PrintWriter outOne = new PrintWriter(clientOne.getOutputStream(), true)) {
            assertTrue("Welcome message", inOne.readLine().contains("Players: 1"));

            try (Socket clientTwo = connectToMinesweeperServer(thread);
                 BufferedReader inTwo = new BufferedReader(new InputStreamReader(clientTwo.getInputStream()));
                 PrintWriter outTwo = new PrintWriter(clientTwo.getOutputStream(), true)) {
                assertTrue("Welcome message", inTwo.readLine().contains("Players: 2"));
                outTwo.println("bye");
            }
            outOne.println("bye");
        }
    }

}
