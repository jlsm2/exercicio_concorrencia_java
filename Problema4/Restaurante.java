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
    private final int CAPACIDADE = 5; // constante
    private final Semaphore cadeirasLivres = new Semaphore(CAPACIDADE, true); // fair=true para garantir que a fila exista (FIFO)
    private final ReentrantLock lock = new ReentrantLock();
    private int pessoasNaMesa = 0; // não possui final pois seu valor muda

    public void entrarRestaurante(int numCliente) throws InterruptedException {
        // não funciona ainda, mas era bom botar
        /*if(!cadeirasLivres.tryAcquire()) {
            System.out.println("Cliente " + numCliente + " tentou entrar, mas o restaurante está cheio. Esperando...");
            cadeirasLivres.acquire(); // como não conseguiu uma cadeira, ele entra na fila de espera
        }*/
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
            pessoasNaMesa--;
            System.out.println("Cliente " + numCliente + " saiu da mesa. Pessoas na mesa: " + pessoasNaMesa);
        } finally {
            lock.unlock(); // saindo da região crítica
        }
        cadeirasLivres.release(); // libera uma cadeira
    }

    private static void criarCliente(Restaurante restaurante, int numCliente) {
        new Thread(() -> {
            try {
                System.out.println("Cliente " + numCliente + " chegou no restaurante.");
                restaurante.entrarRestaurante(numCliente);
                Thread.sleep((long) (Math.random() * 1000)); // quando realmente entrar e sentar no restaurante, depois de um tempo aleatório a pessoa sai
                restaurante.sairRestaurante(numCliente);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws InterruptedException {
        final int NUM_CLIENTES = 100; // número fixo (monitoria)
        Restaurante restaurante = new Restaurante(); // inicializando o restaurante
        for (int i = 0; i < NUM_CLIENTES; i++) {
            criarCliente(restaurante, i); // criando os clientes (threads)
        }
    }
}
