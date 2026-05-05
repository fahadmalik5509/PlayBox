package com.fahadmalik.playbox.dotandboxes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Manages an online Dots-and-Boxes session via Firebase Realtime Database.
 *
 * ── Room structure in Firebase (/dotandboxes/rooms/{roomId}) ──────────────────
 *
 *   roomId          : String
 *   gridSize        : int
 *   timerSeconds    : int        (0 = no timer)
 *   player1Name     : String
 *   player2Name     : String     (null while waiting for opponent)
 *   status          : "waiting" | "playing" | "finished"
 *   currentPlayer   : int        (1 or 2)
 *   hLines          : String     (comma-separated flat array)
 *   vLines          : String     (comma-separated flat array)
 *   boxes           : String     (comma-separated flat array)
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class OnlineGameManager {

    // ── Constants ────────────────────────────────────────────────────────────────

    public static final String STATUS_WAITING  = "waiting";
    public static final String STATUS_PLAYING  = "playing";
    public static final String STATUS_FINISHED = "finished";

    private static final String ROOMS_PATH = "dotandboxes/rooms";

    // ── Callback interfaces ──────────────────────────────────────────────────────

    /** Fired once when the room is successfully created or joined. */
    public interface RoomCallback {
        void onRoomReady(String roomId, int assignedPlayer);
        void onError(String message);
    }

    /** Fired whenever the remote game state changes. */
    @FunctionalInterface
    public interface GameStateCallback {
        void onStateChanged(DotAndBoxesGame game, int currentPlayer, String status,
                            String player1Name, String player2Name);

        /** Override to handle Firebase listener errors. No-op by default. */
        default void onError(String message) {}
    }

    // ── Fields ───────────────────────────────────────────────────────────────────

    private final DatabaseReference db;
    private DatabaseReference roomRef;
    private ValueEventListener stateListener;

    private String roomId;
    private int    myPlayerNumber; // 1 or 2
    private int    gridSize;

    // ── Constructor ──────────────────────────────────────────────────────────────

    public OnlineGameManager() {
        db = FirebaseDatabase.getInstance(
                "https://playbox-by-fahad-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).getReference(ROOMS_PATH);
    }

    // ── Room lifecycle ───────────────────────────────────────────────────────────

    /**
     * Creates a new room and waits for an opponent.
     *
     * @param playerName   Host's display name
     * @param gridSize     Number of boxes per side (e.g. 6 for a 6×6 grid)
     * @param timerSeconds Per-turn countdown; 0 means no timer
     * @param callback     Delivers the roomId and player number (always 1) on success
     */
    public void createRoom(String playerName, int gridSize, int timerSeconds,
                           RoomCallback callback) {
        this.gridSize = gridSize;
        this.myPlayerNumber = 1;
        this.roomId = generateRoomId();

        Map<String, Object> room = new HashMap<>();
        room.put("roomId",       roomId);
        room.put("gridSize",     gridSize);
        room.put("timerSeconds", timerSeconds);
        room.put("player1Name",  playerName);
        room.put("player2Name",  null);
        room.put("status",       STATUS_WAITING);
        room.put("currentPlayer", 1);

        // Flat arrays start as empty strings; Firebase doesn't store nulls well
        DotAndBoxesGame blank = new DotAndBoxesGame(gridSize);
        room.putAll(blank.toFirebaseMap());

        roomRef = db.child(roomId);
        roomRef.setValue(room)
                .addOnSuccessListener(unused -> callback.onRoomReady(roomId, 1))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Joins an existing room as player 2.
     *
     * @param roomId     The 6-character code shown to the host
     * @param playerName Joiner's display name
     * @param callback   Delivers the roomId and player number (always 2) on success
     */
    public void joinRoom(String roomId, String playerName, RoomCallback callback) {
        this.roomId = roomId.toUpperCase().trim();
        this.myPlayerNumber = 2;

        roomRef = db.child(this.roomId);
        roomRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                callback.onError("Room not found. Check the code and try again.");
                return;
            }
            DataSnapshot snap = task.getResult();
            String status = snap.child("status").getValue(String.class);
            if (!STATUS_WAITING.equals(status)) {
                callback.onError("Room is already full or game has ended.");
                return;
            }
            Long gs = snap.child("gridSize").getValue(Long.class);
            this.gridSize = (gs != null) ? gs.intValue() : 6;

            Map<String, Object> update = new HashMap<>();
            update.put("player2Name", playerName);
            update.put("status",      STATUS_PLAYING);

            roomRef.updateChildren(update)
                    .addOnSuccessListener(unused -> callback.onRoomReady(this.roomId, 2))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        });
    }

    // ── Game state ────────────────────────────────────────────────────────────────

    /**
     * Pushes a move to Firebase. The updated game state is broadcast to both players
     * via the listener registered in {@link #listenForStateChanges}.
     *
     * @param game The fully updated game object (after markLine has been called locally)
     */
    public void pushGameState(DotAndBoxesGame game) {
        if (roomRef == null) return;

        Map<String, Object> update = game.toFirebaseMap();

        if (game.isGameOver()) {
            update.put("status", STATUS_FINISHED);
        }
        roomRef.updateChildren(update);
    }

    /**
     * Attaches a persistent listener that fires whenever the room document changes.
     * Automatically reconstructs the {@link DotAndBoxesGame} from the flat arrays.
     */
    public void listenForStateChanges(GameStateCallback callback) {
        if (roomRef == null) return;

        stateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;

                String status  = snap.child("status").getValue(String.class);
                String p1Name  = snap.child("player1Name").getValue(String.class);
                String p2Name  = snap.child("player2Name").getValue(String.class);

                Long cpLong = snap.child("currentPlayer").getValue(Long.class);
                int  cp     = (cpLong != null) ? cpLong.intValue() : 1;

                Long gsLong = snap.child("gridSize").getValue(Long.class);
                int  gs     = (gsLong != null) ? gsLong.intValue() : gridSize;

                String hLines = valueOrEmpty(snap, "hLines");
                String vLines = valueOrEmpty(snap, "vLines");
                String boxes  = valueOrEmpty(snap, "boxes");

                DotAndBoxesGame game = new DotAndBoxesGame(gs);
                game.applyFirebaseState(cp, hLines, vLines, boxes);

                callback.onStateChanged(game, cp, status, p1Name, p2Name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };

        roomRef.addValueEventListener(stateListener);
    }

    /** Removes the Firebase listener. Call this in onDestroy / onStop. */
    public void detachListener() {
        if (roomRef != null && stateListener != null) {
            roomRef.removeEventListener(stateListener);
            stateListener = null;
        }
    }

    /**
     * Marks the room as finished. Use when a player leaves mid-game.
     *
     * @param leavingPlayer Player number of the one who quit
     */
    public void leaveRoom(int leavingPlayer) {
        if (roomRef == null) return;
        Map<String, Object> update = new HashMap<>();
        update.put("status", STATUS_FINISHED);
        update.put("abandonedBy", leavingPlayer);
        roomRef.updateChildren(update);
    }

    /**
     * Restores the manager reference after the Activity is recreated (e.g. screen rotation)
     * or when the game launches from OnlineLobbyActivity with an existing roomId.
     * Does NOT modify Firebase; call {@link #listenForStateChanges} afterwards.
     */
    public void rejoinRoom(String roomId, int playerNumber, int gridSize) {
        this.roomId        = roomId;
        this.myPlayerNumber = playerNumber;
        this.gridSize      = gridSize;
        this.roomRef       = db.child(roomId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    /** Returns a 6-character uppercase alphanumeric room code. */
    private static String generateRoomId() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // omit ambiguous I, O, 0, 1
        Random rng = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }

    @Nullable
    private static String valueOrEmpty(DataSnapshot snap, String key) {
        String val = snap.child(key).getValue(String.class);
        return val != null ? val : "";
    }

    // ── Getters ───────────────────────────────────────────────────────────────────

    public String getRoomId()        { return roomId; }
    public int    getMyPlayerNumber(){ return myPlayerNumber; }
    public int    getGridSize()      { return gridSize; }
}