/*

ônibus com capacidade máxima de 50 pessoas
se o ônibus chegar na parada e não tiver ninguém, ele parte imediatamente
cada passageiro é uma thread
os ônibus passam a cada 1-3 segundos
não tem fila, todos tentam entrar ao mesmo tempo

*/

/*

o que faz o ThreadLocalRandom?
por que volatile?
código nunca para
ônibus não sai assim que fica cheio
mudar o output de quando o ônibus passa e não tem ninguém
por que o join nas threads de ônibus?
criar classe pra quando o ônibus sair
tirar esse runnable
entender esse tempo de chegada dos passageiros
ver se dá pra tirar o String[] args da main
ver se a main depois realmente funciona

 */

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Onibus {
    private static final int CAPACIDADE_MAX = 50; // cabem apenas 50 pessoas nos ônibus
    private final ReentrantLock lock = new ReentrantLock();
    private final Semaphore lugarOnibus = new Semaphore(0); // sem fair=true para não criar fila
    private final Semaphore entrarOnibus = new Semaphore(0); // sem fair=true para não criar fila
    private int passageirosEsperando = 0;
    private boolean passageirosFim = false;

    public void chegadaPassageiro() {
        lock.lock(); // entrando na região crítica
        try {
            passageirosEsperando++;
            System.out.println(Thread.currentThread().getName() + " chegou na parada. Total de passageiros esperando: " + passageirosEsperando);
        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    public void chegadaOnibus() throws InterruptedException{ // throws necessário por causa do .acquire()
        lock.lock(); // entrando na região crítica
        try {
            System.out.println("Ônibus chegou na parada");
            int passageirosNoOnibus = Math.min(passageirosEsperando, CAPACIDADE_MAX); // passageiros que vão embarcar no ônibus que chegou
            if (passageirosNoOnibus > 0) {
                System.out.println(passageirosNoOnibus + "passageiros estão liberados para entrar no ônibus");
                lugarOnibus.release(passageirosNoOnibus); // liberando os lugares do ônibus para os passageiros entrarem
            } else {
                System.out.println("Ninguém está esperando");
            }
        } finally {
            lock.unlock(); // saindo da região crítica
        }


        entrarOnibus.acquire(Math.min(passageirosEsperando, CAPACIDADE_MAX));
        lock.lock(); // entrando na região crítica
        try {
            passageirosEsperando -= Math.min(passageirosEsperando, CAPACIDADE_MAX);
            System.out.println("Ônibus saindo. Passageiros restantes: " + passageirosEsperando);
            if (passageirosEsperando == 0) {
                passageirosFim = true; // a fim de encerrar o código quando não tiver mais ninguém
            }
        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    public void entrarOnibus() throws InterruptedException { // throws necessário por causa do .sleep()
        lugarOnibus.acquire(); // tentando ocupar um lugar no ônibus
        System.out.println(Thread.currentThread().getName() + " está entrando no ônibus");
        Thread.sleep(100); // simulando tempo para entrar no ônibus
        entrarOnibus.release();
    }

    public boolean fim() {
        return passageirosFim;
    }

    public void criarOnibus() {
        Thread threadOnibus = new Thread(() -> { // criando a thread pro ônibus
            try {
                while(!fim()) { // só cria o ônibus se tiver passageiro esperando
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000)); // ônibus passam a cada 1 a 3 segundos
                    chegadaOnibus();
                }
            } catch (InterruptedException e) { // precisa por causa do .sleep()
                Thread.currentThread().interrupt();
            }
        });

        threadOnibus.start(); // começando a thread
        try {
            threadOnibus.join(); // não sei por que
        } catch (InterruptedException e) { // precisa por causa do .join()
            Thread.currentThread().interrupt();
        }
    }

    class Passageiro implements Runnable {
        public void run() {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000)); // simulando o tempo de chegada dos passageiros
                chegadaPassageiro();
                entrarOnibus();
            } catch (InterruptedException e) { // precisa por causa do .sleep()
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        final int quantidadePassageiros = 100; // número fixo pela monitoria
        Onibus parada = new Onibus();

        for(int i = 0; i < quantidadePassageiros; i++) {
            new Thread(parada.new Passageiro()).start(); // criando passageiros
        }

        parada.criarOnibus(); // gerando os ônibus e colocando o sistema pra funcionar
        System.out.println("FIM"); // todos os passageiros sendo atendidos
    }
}
