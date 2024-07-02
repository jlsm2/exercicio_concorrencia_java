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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.Semaphore;

public class Restaurante {
    private final int CAPACIDADE = 5; // constante
    private final Semaphore cadeirasLivres = new Semaphore(CAPACIDADE, true); // fair=true para garantir que a fila exista (FIFO)
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition lugarDisponivel = lock.newCondition(); // nome diferenete para não confundir
    private int pessoasNaMesa = 0; // não possui final pois seu valor muda

    public void entrarRestaurante(int numCliente) throws InterruptedException {
        // tenta adquirir uma cadeira, caso não consiga, entra na fila de espera
        cadeirasLivres.acquire(); // faz com que o throws... seja necessario
        lock.lock(); // adquirindo uma cadeira, entra na região crítica
        try {
            while (pessoasNaMesa == CAPACIDADE) { // enquanto a mesa estiver cheia
                System.out.println("TESTE 1"); // NAO FUNCIONA
                lugarDisponivel.await(); // espera até que um sinal seja enviado
            }
            pessoasNaMesa++;
            System.out.println("Cliente " + numCliente + " sentou na mesa. Pessoas na mesa: " + pessoasNaMesa);

        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    public void sairRestaurante(int numCliente, int CAPACIDADE) {
        lock.lock(); // sempre vai entrar na região crítica
        try {
            pessoasNaMesa--;
            System.out.println("Cliente " + numCliente + " saiu da mesa. Pessoas na mesa: " + pessoasNaMesa);
            if (pessoasNaMesa == CAPACIDADE - 1) { // se a mesa estiver vazia
                //System.out.println("TESTE 2");
                lugarDisponivel.signalAll(); // libera todas as pessoas da fila de espera
            }
        } finally {
            lock.unlock(); // saindo da região crítica
        }
        cadeirasLivres.release(); // libera uma cadeira
    }

    private static void criarCliente(Restaurante restaurante, int numCliente, int CAPACIDADE) {
        new Thread(() -> {
            try {
                System.out.println("Cliente " + numCliente + " chegou no restaurante.");
                restaurante.entrarRestaurante(numCliente);
                Thread.sleep((long) (Math.random() * 1000));
                restaurante.sairRestaurante(numCliente, CAPACIDADE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        Restaurante restaurante = new Restaurante();
        final int NUM_CLIENTES = 10;
        for (int i = 0; i < NUM_CLIENTES; i++) {
            criarCliente(restaurante, i, restaurante.CAPACIDADE);
        }
    }
}
