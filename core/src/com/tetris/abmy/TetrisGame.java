package com.tetris.abmy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

public class TetrisGame extends ApplicationAdapter {

    private static final String TAG = TetrisGame.class.getName();

    private TetrisController tetrisController;
    protected TetrisView tetrisView;

    protected final float gameWidth = 400f;
    private final float gameHeight = 640f;

    public static final String TEXTURE_ATLAS_OBJECTS = "images/helfris.atlas";

    private boolean paused;
    protected int screenWidth;
    protected int screenHeight;

    @Override
    public void create() {

        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        TetrominoManager tetrominoManager = new TetrominoManager();

        tetrisController = new TetrisController(this, tetrominoManager);
        tetrisView = new TetrisView(tetrominoManager, tetrisController, gameWidth, gameHeight);

        paused = false;

    }

    @Override
    public synchronized void render() {
        super.render();

        if (!paused) {
            tetrisController.update();
        }

        tetrisView.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        tetrisView.resize(width, height);
    }

    @Override
    public void pause() {
        super.pause();
        paused = true;
    }

    @Override
    public void resume() {
        super.resume();
        paused = false;
    }

    @Override
    public void dispose() {
        super.dispose();
        tetrisView.dispose();
        tetrisController.dispose();
    }

}
