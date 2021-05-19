package fpdualeveris;

import operators.BallGame;
import operators.Pinball;

/**
 * Clase principal de ejecucion de la aplicacion
 * 
 * @author mferndel
 *
 */
public class FPDual {
	/**
	 * Metodo principal.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		operatorsChallenge();

	}

	/**
	 * Metodo que llama a clases y metodos del paquete operators.
	 */
	private static void operatorsChallenge() {

		BallGame p = Pinball.getInstance();
		/*
		 * Una comparación inutil ya que acabamos de crear 'p' y es del unico tipo de juego, pero si esta clase fuese capaz de ejecutar diferentes juegos que
		 * sigan varias plantillas, si sería útil hacer esta comprobación y podriamos usar esta clase para manejar y ejecutar diferentes juegos.
		 */
		if (p instanceof BallGame)
			p.launchBall();

	}

}
