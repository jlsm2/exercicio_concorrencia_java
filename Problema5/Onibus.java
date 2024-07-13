/*

ônibus com capacidade máxima de 50 pessoas
se o ônibus chegar na parada e não tiver ninguém, ele parte imediatamente
cada passageiro é uma thread
os ônibus passam a cada 1-3 segundos
não tem fila, todos tentam entrar ao mesmo tempo

*/

import java.util.concurrent.locks.ReentrantLock; // para controlar o acesso à região crítica
import java.util.concurrent.Semaphore; // para controlar a quantidade de passageiros que podem entrar no ônibus
import java.util.concurrent.CountDownLatch; // para contar quantos passageiros ainda faltam
import java.util.concurrent.ThreadLocalRandom; // quando várias threads precisam gerar números aleatórios

public class Onibus {
    private static final int CAPACIDADE_MAX = 50; // cabem apenas 50 pessoas nos ônibus
    private final ReentrantLock lock = new ReentrantLock();
    private final Semaphore lugarOnibus = new Semaphore(0); // sem fair=true para não criar fila
    private final Semaphore entrarOnibus = new Semaphore(0); // sem fair=true para não criar fila
    private int passageirosEsperando = 0;
    private final CountDownLatch latch; // poderia ser um booleano, mas precisaria usar volatile para que as threads vissem a mudança

    public Onibus(int totalPassageiros) { // inicializando variáveis que dependem do número total de passageiros
        this.latch = new CountDownLatch(totalPassageiros);
    }

    public void chegadaPassageiro() {
        lock.lock(); // entrando na região crítica
        try {
            passageirosEsperando++;
            System.out.println(Thread.currentThread().getName() + " chegou na parada. Total de passageiros esperando: " + passageirosEsperando);
        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    public void chegadaOnibus() {
        lock.lock(); // entrando na região crítica
        try {
            System.out.println("Ônibus chegou na parada");
            int passageirosNoOnibus = Math.min(passageirosEsperando, CAPACIDADE_MAX); // passageiros que vão embarcar no ônibus que chegou
            if (passageirosNoOnibus > 0) {
                System.out.println(passageirosNoOnibus + " passageiros estão liberados para entrar no ônibus");
                lugarOnibus.release(passageirosNoOnibus); // liberando os lugares do ônibus para os passageiros entrarem
            } else {
                System.out.println("Ninguém está esperando");
            }
        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    public void saidaOnibus() throws InterruptedException { // throws necessário por causa do .acquire()
        entrarOnibus.acquire(Math.min(passageirosEsperando, CAPACIDADE_MAX));
        lock.lock(); // entrando na região crítica
        try {
            passageirosEsperando -= Math.min(passageirosEsperando, CAPACIDADE_MAX);
            System.out.println("Ônibus saindo. Passageiros restantes: " + passageirosEsperando);
        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    public void entrarOnibus() throws InterruptedException { // throws necessário por causa do .sleep()
        lugarOnibus.acquire(); // tentando ocupar um lugar no ônibus
        System.out.println(Thread.currentThread().getName() + " está entrando no ônibus");
        entrarOnibus.release();
    }

    public void criarOnibus() {
        Thread threadOnibus = new Thread(() -> { // criando a thread pro ônibus
            try {
                while (latch.getCount() > 0) { // enquanto ainda tiver passageiros pra entrar
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000)); // ônibus passam a cada 1 a 3 segundos
                    chegadaOnibus();
                }
            } catch (InterruptedException e) { // precisa por causa do .sleep()
                Thread.currentThread().interrupt();
            }
        });

        threadOnibus.start(); // começando a thread
    }

    class Passageiro {
        public void executarPassageiro() {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000)); // simulando o tempo de chegada dos passageiros
                chegadaPassageiro();
                entrarOnibus();

                lock.lock(); // entrando na região crítica
                try {
                    latch.countDown(); // decrementando o latch
                } finally {
                    lock.unlock(); // saindo da região crítica
                }
            } catch (InterruptedException e) { // precisa por causa do .sleep()
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        final int quantidadePassageiros = 100; // número fixo pela monitoria
        Onibus parada = new Onibus(quantidadePassageiros);

        for (int i = 0; i < quantidadePassageiros; i++) {
            final Passageiro passageiro = parada.new Passageiro();
            Thread passageiroThread = new Thread(() -> passageiro.executarPassageiro());
            passageiroThread.setName("Passageiro " + (i+1));
            passageiroThread.start();
        }

        parada.criarOnibus(); // gerando os ônibus e colocando o sistema pra funcionar
    }
}
