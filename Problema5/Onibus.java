/*

ônibus com capacidade máxima de 50 pessoas
se o ônibus chegar na parada e não tiver ninguém, ele parte imediatamente
cada passageiro é uma thread
os ônibus passam a cada 1-3 segundos
não tem fila, todos tentam entrar ao mesmo tempo

*/

import java.util.concurrent.locks.ReentrantLock; // para controlar o acesso à região crítica
import java.util.concurrent.Semaphore; // para controlar a quantidade de passageiros que podem entrar no ônibus
import java.util.concurrent.ThreadLocalRandom; // quando várias threads precisam gerar números aleatórios

public class Onibus {
    private static final int CAPACIDADE_MAX = 50; // cabem apenas 50 pessoas nos ônibus
    private final ReentrantLock lock = new ReentrantLock();
    private final Semaphore lugarOnibus = new Semaphore(0); // sem fair=true para não criar fila
    private final Semaphore entrarOnibus = new Semaphore(0); // sem fair=true para não criar fila
    private int passageirosEsperando = 0;
    private final int totalOnibus;
    private int onibusChegaram = 0; // para registrar o número dos ônibus

    public Onibus(int totalOnibus) { // inicializando essa variável
        this.totalOnibus = totalOnibus;
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

    public void chegadaOnibus() throws InterruptedException { // throws... necessário por causa do .sleep()
        lock.lock(); // entrando na região crítica
        try {
            if (onibusChegaram >= totalOnibus) { // para não permitir mais ônibus se o limite for atingido
                return;
            }
            onibusChegaram++; // aumentando o número do ônibus
            System.out.println("Ônibus chegou na parada. Ônibus número: " + onibusChegaram);
            int passageirosNoOnibus = Math.min(passageirosEsperando, CAPACIDADE_MAX); // entram no ônibus ou 50 pessoas (máximo) ou menos
            if (passageirosNoOnibus > 0) {
                System.out.println(passageirosNoOnibus + " passageiros estão liberados para entrar no ônibus");
                lugarOnibus.release(passageirosNoOnibus);
                Thread.sleep(1000); // tempo para os passageiros entrar no ônibus
                saidaOnibus(); // chamando a função para o ônibus sair
            } else {
                System.out.println("Ninguém está esperando"); // quando não houver ninguém na parada
                System.out.println("Ônibus partindo vazio");
            }
        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    public void saidaOnibus() throws InterruptedException { // throws... necessário por causa do .acquire()
        entrarOnibus.acquire(Math.min(passageirosEsperando, CAPACIDADE_MAX)); // tenta conseguir o semáforo para os passageiros que vão entrar no ônibus
        lock.lock(); // conseguindo ele, entra na região crítica
        try {
            passageirosEsperando -= Math.min(passageirosEsperando, CAPACIDADE_MAX);
            System.out.println("Ônibus saindo. Passageiros restantes: " + passageirosEsperando);
        } finally {
            lock.unlock(); // saindo da região crítica
        }
    }

    public void entrarOnibus() throws InterruptedException { // throws... necessário por causa do .acquire()
        lugarOnibus.acquire(); // tenta conseguir o semáforo dos lugares
        System.out.println(Thread.currentThread().getName() + " está entrando no ônibus");
        entrarOnibus.release(); // solta o lugar do ônibus
    }

    public void criarOnibus() {
        Thread threadOnibus = new Thread(() -> {
            try {
                while (onibusChegaram < totalOnibus) { // cria um certo número de ônibus
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000)); // sempre entre 1 e 3 segundos
                    chegadaOnibus();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        threadOnibus.start(); // inicia a thread
    }

    class Passageiro {
        public void executarPassageiro() {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000)); // simulando tempo de chegada
                chegadaPassageiro();
                entrarOnibus();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        final int quantidadePassageiros = 100;
        final int quantidadeOnibus = 5; // número arbitrário pra testar quando não houver ninguém na parada
        Onibus parada = new Onibus(quantidadeOnibus); // criando a parada

        for (int i = 0; i < quantidadePassageiros; i++) {
            final Passageiro passageiro = parada.new Passageiro(); // criando o passageiro
            Thread passageiroThread = new Thread(() -> passageiro.executarPassageiro()); // executando o passageiro
            passageiroThread.setName("Passageiro " + (i+1));
            passageiroThread.start(); // iniciando o passageiro
        }

        parada.criarOnibus();
    }
}
