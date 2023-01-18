package com.divtec.blatnoa.snakegame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.divtec.blatnoa.snakegame.Snake.Snake;
import com.divtec.blatnoa.snakegame.Tick.TickManager;

public class SnakeActivity extends AppCompatActivity {

    private final int CELL_COLUMN_COUNT = 15;

    private ConstraintLayout lyt;
    private Button restartButton;
    private GridLayout grid;
    private TextView scoreText;

    private Snake snake;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener acceleroListener;

    private boolean hasStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);

        // Lock screen orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Get components
        lyt = findViewById(R.id.layout);
        grid = findViewById(R.id.cellGrid);
        restartButton = findViewById(R.id.restartButton);
        scoreText = findViewById(R.id.scoreText);

        // Set text to 0
        scoreText.setText("0");

        // Set up button listener
        restartButton.setOnClickListener(view -> {
            // Restart activity
            recreate();
        });

        // Get sensor manager and from it the rotation sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Create a listener for the rotation sensor
        acceleroListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(android.hardware.SensorEvent event) {
                // If the game has not started
                if (!hasStarted) {
                    // Ignore the event
                    return;
                }

                // Get highest reading above 0.5
                float x = event.values[0];
                float y = event.values[1];

                float absX = Math.abs(x);
                float absY = Math.abs(y);
                float maxAbs = Math.max(absX, absY);

                // If the highest reading is below 2.5, ignore
                if (maxAbs < 2.5) {
                    return;
                }

                if (maxAbs == absX) { // If the highest reading is on the X axis
                    if (x > 0) { // If the reading is positive
                        snake.turn(Snake.Direction.DOWN);
                    } else { // If the reading is negative
                        snake.turn(Snake.Direction.UP);
                    }
                } else { // If the highest reading is on the Y axis
                    if (y > 0) { // If the reading is positive
                        snake.turn(Snake.Direction.RIGHT);
                    } else { // If the reading is negative
                        snake.turn(Snake.Direction.LEFT);
                    }
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        // Generate the grid when layout is ready
        lyt.getViewTreeObserver().addOnGlobalLayoutListener(this::prepareLayout);
    }

    /**
     * Generates the grid according to the size of the layout and the requested cell size
     */
    private void prepareLayout() {
        if (grid.getChildCount() == 0) {

            // Get layout size
            int width = grid.getWidth();
            int height = grid.getHeight();

            // Get cell size
            int cellSize = width / CELL_COLUMN_COUNT;

            // Set grid size
            grid.setColumnCount(CELL_COLUMN_COUNT);
            grid.setRowCount(height / cellSize);

            // Center the grid with padding in layout
            int xPadding = width % cellSize / 2;
            int yPadding = height % cellSize / 2;
            lyt.setPadding(xPadding, yPadding, xPadding, yPadding);

            // Generate cells
            boolean cellColor1 = true;
            boolean switchOnNewRow = grid.getColumnCount() % 2 == 0;
            for (int i = 0; i < grid.getRowCount(); i++) {
                for (int j = 0; j < grid.getColumnCount(); j++) {
                    ImageView cell = new ImageView(this);
                    cell.setAdjustViewBounds(true);
                    cell.setMinimumWidth(cellSize);
                    cell.setMinimumHeight(cellSize);
                    cell.setMaxWidth(cellSize);
                    cell.setMaxHeight(cellSize);

                    // Alternate background between cells
                    cell.setBackgroundColor(getColor(cellColor1 ? R.color.cell1 : R.color.cell2));

                    // Add the cell to the grid
                    grid.addView(cell);

                    cellColor1 = !cellColor1;
                }
                // If the grid has an uneven number of columns
                if (switchOnNewRow) {
                    // Alternate the first cell type on each row
                    cellColor1 = !cellColor1;
                }
            }

            // Create the snake
            snake = new Snake(grid);
            startGame();
        }
    }

    /**
     * Starts the game
     */
    private void startGame() {
        // Register the listener
        sensorManager.registerListener(acceleroListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        hasStarted = true;
        TickManager.getTickManager().start();
    }

    /**
     * End snake game
     */
    public void gameOver() {
        grid.setTranslationZ(-5);
        for (int i = 0; i < grid.getChildCount(); i++) {
            ImageView cell = (ImageView) grid.getChildAt(i);
            cell.setBackgroundColor(0);
        }
        TickManager.getTickManager().stop();
    }

    /**
     * Updates the score
     * @param score The new score
     */
    public void updateScore(int score) {
        runOnUiThread(() -> {
            String baseText = Integer.toString(score);
            StringBuilder finalText = new StringBuilder("Score:");

            for (int i = 0; i < baseText.length(); i++) {
                finalText.append(baseText.charAt(i)).append("/n");
            }

            scoreText.setText(finalText);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener
        sensorManager.unregisterListener(acceleroListener);
        TickManager.getTickManager().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the listener
        sensorManager.registerListener(acceleroListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        TickManager.getTickManager().resume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the listener
        sensorManager.unregisterListener(acceleroListener);
        TickManager.getTickManager().stop();
    }
}