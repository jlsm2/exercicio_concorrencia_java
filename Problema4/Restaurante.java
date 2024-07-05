/*
Antes de adicionar pessoa na mesa:
    Checar se a mesa está completa
        Se estiver completa, adicionar pessoa na fila de espera
        Se não estiver completa, adicionar pessoa na mesa
Depois de adicionar pessoa na mesa:
    Checar se a mesa está completa
        Se estiver completa, liberar todas as pessoas da fila de espera
        Se não estiver completa, não fazer nada
 */

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;

public class Restaurante {
    private final int CAPACIDADE = 5;
    private final Semaphore cadeirasLivres = new Semaphore(CAPACIDADE, true); // usando fair=true para que FIFO seja garantido
    private final ReentrantLock lock = new ReentrantLock();
    private int pessoasNaMesa = 0;
    private int queremSair = 0;

    public void entrarRestaurante(int numCliente) throws InterruptedException { // throws... necessário por causa do acquire
        cadeirasLivres.acquire(); // tenta adquirir uma cadeira, caso não consiga, entra na fila de espera
        lock.lock(); // adquirindo uma cadeira, entra na região crítica
        try {
            pessoasNaMesa++;
            System.out.println("Cliente " + numCliente + " sentou na mesa. Pessoas na mesa: " + pessoasNaMesa);
        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    public void sairRestaurante(int numCliente) {
        lock.lock(); // sempre vai entrar na região crítica
        try {
            queremSair++;
            System.out.println("Cliente " + numCliente + " quer sair. Pessoas querendo sair: " + queremSair);

            if (queremSair == pessoasNaMesa) { // se todos quiserem sair
                System.out.println("Todos os clientes estão saindo da mesa.");
                pessoasNaMesa = 0; // resetar as variáveis
                queremSair = 0;
                cadeirasLivres.release(CAPACIDADE); // liberando todas as cadeiras
            }
        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    private static void criarCliente(Restaurante restaurante, int numCliente) {
        new Thread(() -> {
            try {
                System.out.println("Cliente " + numCliente + " chegou no restaurante.");
                restaurante.entrarRestaurante(numCliente);
                Thread.sleep((long) (Math.random() * 1000)); // introduzindo aleatoriedade
                restaurante.sairRestaurante(numCliente);
            } catch (InterruptedException e) { // necessário por causa do sleep
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        Restaurante restaurante = new Restaurante(); // inicializando o restaurante
        final int NUM_CLIENTES = 20; // número fixo de todas as questões
        for (int i = 0; i < NUM_CLIENTES; i++) {
            criarCliente(restaurante, i);
        }
    }
}
