package rosalila.studio.workcamp;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.PathModifier;
import org.andengine.entity.modifier.PathModifier.IPathModifierListener;
import org.andengine.entity.modifier.PathModifier.Path;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 19:58:39 - 19.07.2010
 */
public class MainActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mPlayerTextureRegion;
	private TiledTextureRegion mChuyTextureRegion;

	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;

	private ITextureRegion mParallaxLayerBack;
	private ITextureRegion mParallaxLayerMid;
	private ITextureRegion mParallaxLayerFront;
	
	public AnimatedSprite player;
	public Body body;
	PhysicsWorld mPhysicsWorld;
	public Rectangle rect;
	public AnimatedSprite chuy;
	
	private BitmapTextureAtlas gameover_texture_atlas;
	private ITextureRegion gameover_texture_region;
	private Sprite gameover_background;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "player.png", 0, 0, 3, 4);
		this.mChuyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "chuy.png", 73, 0, 3, 4);
		this.mBitmapTextureAtlas.load();

		this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
		this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_front.png", 0, 0);
		this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_back.png", 0, 188);
		this.mParallaxLayerMid = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_mid.png", 0, 669);
		this.mAutoParallaxBackgroundTexture.load();
		
		this.gameover_texture_atlas = new BitmapTextureAtlas(this.getTextureManager(), 320, 230, TextureOptions.BILINEAR);
		this.gameover_texture_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.gameover_texture_atlas, this, "game_over_bg.png", 0, 0);
		this.gameover_texture_atlas.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), this.mParallaxLayerBack, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.mParallaxLayerMid, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager)));
		scene.setBackground(autoParallaxBackground);

		/* Calculate the coordinates for the face, so its centered on the camera. */
		final float playerX = (CAMERA_WIDTH - this.mPlayerTextureRegion.getWidth()) / 2;
		final float playerY = CAMERA_HEIGHT - this.mPlayerTextureRegion.getHeight() - 5;

		/* Create two sprits and add it to the scene. */
		player = new AnimatedSprite(playerX, playerY, this.mPlayerTextureRegion, vertexBufferObjectManager);
		player.setScaleCenterY(this.mPlayerTextureRegion.getHeight());
		player.setScale(2);
		player.animate(new long[]{200, 200, 200}, 3, 5, true);
		
		//Create physics body
		this.mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, (float) 9.8), false, 8, 1);
		scene.registerUpdateHandler(this.mPhysicsWorld);
		final FixtureDef playerFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0.5f);
		this.body = PhysicsFactory.createBoxBody(mPhysicsWorld, player, BodyType.DynamicBody, playerFixtureDef);
        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(player, body, true, false){
            @Override
            public void onUpdate(float pSecondsElapsed){
                    super.onUpdate(pSecondsElapsed);
                    if(player.collidesWith(chuy))
                    {
                    	chuy.clearEntityModifiers();
                    	body.setLinearVelocity(0,0);
                    	gameover_background.setVisible(true);
                    }else
                    {
                    	gameover_background.setVisible(false);
                    }
            }
        });
        
        //Create floor
        rect = new Rectangle(0, CAMERA_HEIGHT, CAMERA_WIDTH, 1, getVertexBufferObjectManager());
        final FixtureDef boxFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 1f);
        PhysicsFactory.createBoxBody(mPhysicsWorld, rect, BodyType.StaticBody, boxFixtureDef);
        scene.attachChild(rect);

        //Create enemy
		chuy = new AnimatedSprite(playerX - 80, playerY, this.mChuyTextureRegion, vertexBufferObjectManager);
		//enemy.setScaleCenterY(this.mEnemyTextureRegion.getHeight());
		chuy.setScale((float)0.5);
		chuy.animate(new long[]{200, 200, 200}, 3, 5, true);

		final Path path = new Path(3).to(10, CAMERA_HEIGHT - chuy.getHeight()).to(CAMERA_WIDTH - 58, CAMERA_HEIGHT - chuy.getHeight()).to(10, CAMERA_HEIGHT - chuy.getHeight());

		/* Add the proper animation when a waypoint of the path is passed. */
		chuy.registerEntityModifier(new LoopEntityModifier(new PathModifier(10, path, null, new IPathModifierListener() {

			@Override
			public void onPathStarted(PathModifier pPathModifier,
					IEntity pEntity) {
			}

			@Override
			public void onPathWaypointStarted(PathModifier pPathModifier,
					IEntity pEntity, int pWaypointIndex) {
			}

			@Override
			public void onPathWaypointFinished(PathModifier pPathModifier,
					IEntity pEntity, int pWaypointIndex) {
			}

			@Override
			public void onPathFinished(PathModifier pPathModifier,
					IEntity pEntity) {
			}
		})));
		
		//Add game over image
		final float centerX = (CAMERA_WIDTH - this.gameover_texture_region.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.gameover_texture_region.getHeight()) / 2;
		this.gameover_background = new Sprite(centerX, centerY, this.gameover_texture_region, this.getVertexBufferObjectManager())
		{
			@Override
			public boolean onAreaTouched(
					TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub
				if(this.isVisible())
				{
					chuy.setPosition(10, CAMERA_HEIGHT - 74);
					final Path path = new Path(3).to(10, CAMERA_HEIGHT - chuy.getHeight()).to(CAMERA_WIDTH - 58, CAMERA_HEIGHT - chuy.getHeight()).to(10, CAMERA_HEIGHT - chuy.getHeight());

					/* Add the proper animation when a waypoint of the path is passed. */
					chuy.registerEntityModifier(new LoopEntityModifier(new PathModifier(10, path, null, new IPathModifierListener() {

						@Override
						public void onPathStarted(PathModifier pPathModifier,
								IEntity pEntity) {
						}

						@Override
						public void onPathWaypointStarted(PathModifier pPathModifier,
								IEntity pEntity, int pWaypointIndex) {
						}

						@Override
						public void onPathWaypointFinished(PathModifier pPathModifier,
								IEntity pEntity, int pWaypointIndex) {
						}

						@Override
						public void onPathFinished(PathModifier pPathModifier,
								IEntity pEntity) {
						}
					})));
				}
				return super
						.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
			}
		};
		scene.registerTouchArea(gameover_background);
		scene.attachChild(this.gameover_background);
		gameover_background.setVisible(false);
		
		
		scene.attachChild(player);
		scene.attachChild(chuy);
		
		
		
		
		scene.setOnSceneTouchListener(this);

		return scene;
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(player.getY()>CAMERA_HEIGHT-50)
			body.setLinearVelocity(0,-12);
		return false;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}