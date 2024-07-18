/*

ônibus com capacidade máxima de 50 pessoas
se o ônibus chegar na parada e não tiver ninguém, ele parte imediatamente
cada passageiro é uma thread
os ônibus passam a cada 1-3 segundos
não tem fila, todos tentam entrar ao mesmo tempo

*/

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class Onibus {
    private static final int CAPACIDADE_MAX = 50;
    private final ReentrantLock lock = new ReentrantLock();
    private final Semaphore lugarOnibus = new Semaphore(0);
    private final Semaphore entrarOnibus = new Semaphore(0);
    private int passageirosEsperando = 0;
    private final int totalOnibus;
    private int onibusChegaram = 0;

    public Onibus(int totalOnibus) {
        this.totalOnibus = totalOnibus;
    }

    public void chegadaPassageiro() {
        lock.lock();
        try {
            passageirosEsperando++;
            System.out.println(Thread.currentThread().getName() + " chegou na parada. Total de passageiros esperando: " + passageirosEsperando);
        } finally {
            lock.unlock();
        }
    }

    public void chegadaOnibus() throws InterruptedException {
        lock.lock();
        try {
            if (onibusChegaram >= totalOnibus) {
                return; // Não permita mais ônibus se o limite foi atingido
            }
            onibusChegaram++;
            System.out.println("Ônibus chegou na parada. Ônibus número: " + onibusChegaram);
            int passageirosNoOnibus = Math.min(passageirosEsperando, CAPACIDADE_MAX);
            if (passageirosNoOnibus > 0) {
                System.out.println(passageirosNoOnibus + " passageiros estão liberados para entrar no ônibus");
                lugarOnibus.release(passageirosNoOnibus);
                Thread.sleep(1000);
                saidaOnibus();
            } else {
                System.out.println("Ninguém está esperando");
            }
        } finally {
            lock.unlock();
        }
    }

    public void saidaOnibus() throws InterruptedException {
        entrarOnibus.acquire(Math.min(passageirosEsperando, CAPACIDADE_MAX));
        lock.lock();
        try {
            passageirosEsperando -= Math.min(passageirosEsperando, CAPACIDADE_MAX);
            System.out.println("Ônibus saindo. Passageiros restantes: " + passageirosEsperando);
        } finally {
            lock.unlock();
        }
    }

    public void entrarOnibus() throws InterruptedException {
        lugarOnibus.acquire();
        System.out.println(Thread.currentThread().getName() + " está entrando no ônibus");
        entrarOnibus.release();
    }

    public void criarOnibus() {
        Thread threadOnibus = new Thread(() -> {
            try {
                while (onibusChegaram < totalOnibus) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
                    chegadaOnibus();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        threadOnibus.start();
    }

    class Passageiro {
        public void executarPassageiro() {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000));
                chegadaPassageiro();
                entrarOnibus();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        final int quantidadePassageiros = 100;
        final int quantidadeOnibus = 5;
        Onibus parada = new Onibus(quantidadeOnibus);

        for (int i = 0; i < quantidadePassageiros; i++) {
            final Passageiro passageiro = parada.new Passageiro();
            Thread passageiroThread = new Thread(() -> passageiro.executarPassageiro());
            passageiroThread.setName("Passageiro " + (i+1));
            passageiroThread.start();
        }

        parada.criarOnibus();
    }
}
