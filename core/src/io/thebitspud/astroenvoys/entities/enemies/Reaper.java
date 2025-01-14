package io.thebitspud.astroenvoys.entities.enemies;

import com.badlogic.gdx.Gdx;

import java.util.Random;

import io.thebitspud.astroenvoys.AstroEnvoys;
import io.thebitspud.astroenvoys.entities.EntityID;
import io.thebitspud.astroenvoys.entities.Player;
import io.thebitspud.astroenvoys.tools.JTimerUtil;

public class Reaper extends Enemy {
	private final Random r;
	private final JTimerUtil mainAttackTimer, burstAttackTimer, summonTimer;
	private final Player player;
	private int rotYPos, nextSpawnHP;
	private boolean secondStageActive;
	private float yLimit, dx, dy;

	public Reaper(int x, int y, AstroEnvoys app) {
		super(x, y, 0, -80, 800, EntityID.AZ_REAPER, app);

		r = new Random();
		player = app.gameScreen.game.player;
		cYOffset = -75;
		setOrigin(getWidth() / 2, (getHeight() / 2 + cYOffset));
		rotYPos = (int) (getY() + getOriginY());
		yLimit = Gdx.graphics.getHeight() * 0.75f;

		secondStageActive = false;
		nextSpawnHP = health;

		mainAttackTimer = new JTimerUtil(0.25, true, true) {
			@Override
			public void onActivation() {
				final int yAdjust = -20;
				double hyp = Math.hypot(dx, dy + yAdjust);
				float scale = (float) (1200 / hyp);
				final float xv = dx * scale, yv = (dy + yAdjust) * scale;

				app.gameScreen.game.addProjectile((int) getX() + 78, rotYPos + yAdjust, xv, yv,
						EntityID.PLASMA_SHOT);
			}
		};

		burstAttackTimer = new JTimerUtil(1.25, true, true) {
			@Override
			public void onActivation() {
				final int yAdjust = -20;
				double hyp = Math.hypot(dx, dy + yAdjust);
				float scale = (float) (1500 / hyp);
				final float xv = dx * scale, yv = (dy + yAdjust) * scale;

				app.gameScreen.game.addProjectile((int) getX() + 71, rotYPos + yAdjust, xv, yv,
						EntityID.HEAVY_PLASMA_SHOT);

				// Linear algebra trick lol
				float s = (float) Math.sin(0.25), c = (float) Math.cos(0.25);
				app.gameScreen.game.addProjectile((int) getX() + 71, rotYPos + yAdjust,
						xv * c - yv * s, xv * s + yv * c, EntityID.HEAVY_PLASMA_SHOT);

				app.gameScreen.game.addProjectile((int) getX() + 71, rotYPos + yAdjust,
						xv * c + yv * s, yv * c - xv * s,
						EntityID.HEAVY_PLASMA_SHOT);
			}
		};

		summonTimer = new JTimerUtil(15, true, true) {
			@Override
			public void onActivation() {
				app.gameScreen.game.spawnEnemy(r.nextInt(Gdx.graphics.getWidth() - 100), y, EntityID.AZ_RAIDER);
			}
		};

		app.gameScreen.game.level.addTimer(summonTimer);
	}

	@Override
	protected void tickAI(float delta) {
		mainAttackTimer.tick(delta);
		burstAttackTimer.tick(delta);

		if(yVel != 0 && getY() <= yLimit) yVel = 0;

		rotYPos = (int) (getY() + getOriginY());
		dx = (player.getX() + player.getWidth() / 2) - (getX() + getWidth() / 2);
		dy = (player.getY() + player.getHeight() / 2) - rotYPos;
		final double degrees = (Math.atan2(dx, -dy) * 180.0 / Math.PI);
		setRotation((float) degrees);
	}

	@Override
	public void adjustHealth(int value) {
		health += value;

		if(!secondStageActive && health <= 400) {
			yLimit = Gdx.graphics.getHeight() * 0.6f;
			mainAttackTimer.setTimerDuration(0.2);
			burstAttackTimer.setTimerDuration(1.0);
			secondStageActive = true;
			yVel = -80;
		}

		if(health <= nextSpawnHP) {
			summonTimer.setTimeElapsed(10 + summonTimer.getTimeElapsed());
			nextSpawnHP -= 80;
		}

		if (health > maxHealth) health = maxHealth;
		else if (health <= 0) {
			health = 0;
			app.gameScreen.game.level.removeTimer(summonTimer);
			kill();
		}
	}
}
