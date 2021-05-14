package operators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Clase que describe un juego de pinball automatico basado en numeros aleatorios. Singleton
 * 
 * @author mferndel
 *
 */
public class Pinball implements BallGame {

	/** Objeto Random con el que vamos a generar toda la aleatoriedad de la clase */
	private final Random rand = new Random();
	/** Puntos totales ganados en la partida */
	private int totalPoints;
	/** Probabilidad de golpear un obstaculo del pinball en lugar de caerse y perder */
	private float bumpHitChance;
	/** Unica instancia de la clase */
	private static Pinball instance = null;
	/** Timer que va generando eventos del juego cada poco tiempo */
	private final Timer gameTimer = new Timer();
	/** Ruta del archivo donde se guardan las puntuaciones mas altas*/
	private final Path HIGHSCOREPATH = Path.of("highscore.dat");
	/** Lista con las puntuaciones mas altas del juego*/
	private List<Integer> highscores;
	/**
	 * Constructor privado que solo se llama al instanciar el unico objeto del singleton
	 */
	private Pinball() {
		totalPoints = 0;
		bumpHitChance = 1f;
		highscores = new ArrayList<Integer>();
		try {
			Files.lines(HIGHSCOREPATH).map(Integer::parseInt).forEach(highscores::add);
		} catch (IOException e) {
			System.err.println("bad highscore load");
		}		
	}

	/**
	 * Metodo static de la clase que devuelve la instancia del singleton
	 * 
	 * @return
	 */
	public static Pinball getInstance() {
		if (instance == null)
			instance = new Pinball();

		return instance;
	}
	
	

	/**
	 * Metodo que inicia el juego. En el juego se simula que la bola esta rebotando alrededor de las paredes y diferentes obstaculos, llamando periodicamente a
	 * un timerTask para que ejecute un accion. La accion puede resultar en otro rebote o en perder, en funcion de un numero aleatorio y bumphitChance
	 */
	public void launchBall() {

		TimerTask bumpTask = new TimerTask() {
			/**
			 * Metodo abstracto de TimerTask que debo implementar para usar la clase Timer
			 */
			@Override
			public void run() {
				if (bumpHitChance > rand.nextFloat()) {
					bump();
				} else {
					fail();
				}
			}
		};

		gameTimer.schedule(bumpTask, 0, 1000);

	}

	/**
	 * Simula el rebote de la bola con un componente del pinball. Cada componente da una puntuacion distinta al golpearlo. Algunos tienen un comportamiento mas
	 * elaborado. Todos afectan a la probabilidad de rebotar en el siguiente turno o de caer y perder el juego.
	 */
	private void bump() {
		int bumpType = rand.nextInt(15);
		int points = 0;
		switch (bumpType) {
		case 0, 1, 2, 3, 4:
			bumpHitChance -= .15f;
			points = 1500;
			break;

		case 5, 6, 7, 8:
			bumpHitChance += .1f;
			points = 1000;
			break;
		case 9, 10, 11:
			points = spikeTrap();
			break;
		case 12, 13:
			points = bumperTrap();
			break;
		case 14:
			points = jackpot();
			break;
		}
		System.out.println("BUMP! you get " + points + " (fail Chance: " + failChance() + "%)");
		totalPoints += points;
	}

	/** la bola ha chocado contra el componente que da mas puntos potencialmente, ademas asegura que el proximo lanzamiento no falle */
	private int jackpot() {
		int points = 0;
		System.out.println("Amazing!! you hit jackpot!!");
		while (bumpHitChance < 1f || points < 3000) {
			System.out.println("DING");
			points += 1000 + totalPoints / 10;
			bumpHitChance += .1f;
		}
		return points;
	}

	/**
	 * Metodo que calcula los puntos restados al "chocar" con un componente. Al chocar con esta trampa se pierde un porcentaje variable de tus puntos totales,
	 * pero nunca resta mas del ~33%
	 * 
	 * @return puntuacion restada
	 */
	private int spikeTrap() {
		bumpHitChance -= .25f;
		int points = 0;
		int spikes = rand.nextInt(3) + 1;
		System.out.println("OH NO Spike Trap!!");
		for (int i = 1; i <= spikes; i++) {
			points += totalPoints / (4 ^ i);
		}
		System.out.println("OUCH You lose " + points + " points");
		return -points;
	}

	/**
	 * Calcula los puntos ganados al entrar en un componente. Imprime por pantalla sonidos como si estuviese rebotando muchas veces, y cada 'rebote rapido' da
	 * una cantidad aleatoria de puntos entre 100 y 200. No puede dar menos puntos que 500, y tras cada rebote rápido tiene una probabilidad del 50% de escapar
	 * y volver a juego normal
	 * 
	 * @return
	 */
	private int bumperTrap() {
		bumpHitChance += .2f;
		int points = 0;
		boolean gotOut = false;
		System.out.println("WOW You got yourself into the bumper trap!");
		String[] sounds = { "CLINK", "CLONK", "CLUNK", "BONK", "CLANK" };
		do {
			points += rand.nextInt(100) + 100;
			System.out.println(sounds[rand.nextInt(5)]);
			gotOut = rand.nextBoolean();
		} while (points < 500 || !gotOut);
		return points;
	}

	/**
	 * Metodo que se invoca al perder el juego, muestra los puntos totales y detiene el Timer.
	 */
	private void fail() {
		System.out.println("GameOver!");
		System.out.println("Total Points: " + totalPoints);
		gameTimer.cancel();
		updateHighscore();
	}
/**
 *  Actualiza las puntuaciones y muestra las mas altas, pues se invoca solo al terminar el juego.
 */
	private void updateHighscore() {
		highscores.add(totalPoints);
		Collections.sort(highscores, Collections.reverseOrder());
		
		System.out.println("Highscore Board:");	
		highscores.subList(0, 5).forEach(System.out::println);
		
		
		List<String> stringList = highscores.stream().map(String::valueOf).collect(Collectors.toList());
		try {
			Files.write(HIGHSCOREPATH, stringList);
		} catch (IOException e) {
			System.err.println("bad highscore save");
		}
	}

	/**
	 * Devuelve la probabilidad de fallar, deducida trivialmente de la probabilidad de no fallar, y la formatea para ser mas legible por pantalla.
	 * 
	 * @return
	 */
	private int failChance() {
		int res = (int) (100f - (bumpHitChance * 100f));
		return (res > 0) ? res : 0;
	}

}
