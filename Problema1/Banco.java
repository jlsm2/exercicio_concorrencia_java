/*
ORGANIZAÇÃO DO CÓDIGO

Classe Conta:
Função que inicializa a conta
Função que deposita dinheiro na conta (depósito)
Função que tira dinheiro da conta (saque)
Função que vê quanto dinheiro tem na conta

Classe Cliente:
Função que inicializa o cliente
Função que chama a função de depósito na conta
Função que chama a função de saque na conta

Classe Main:
Inicializar vários clientes (threads)
Clientes botando e tirando dinheiro

*/

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

class Conta {
    private double saldo;
    private final ReentrantLock lock;

    public Conta(double saldoInicial) { // inicializando a conta
        this.saldo = saldoInicial;
        this.lock = new ReentrantLock();
    }

    public void deposito(double valor) throws InterruptedException { // throw pra conseguir usar o Thread.sleep
        Thread.sleep(1000); // esperando um tempo antes de entrar na seção crítica, simulando um atraso, ajudando na visualização da concorrência
        lock.lock(); // entrando na seção crítica
        saldo += valor;
        System.out.println(Thread.currentThread().getName() + " depositou " + valor + " na conta. Saldo atual: " + saldo);
        lock.unlock(); // saindo da seção crítica
    }

    public void saque(double valor) throws InterruptedException { // throw pra conseguir usar o Thread.sleep
        Thread.sleep(1000); // esperando um tempo antes de entrar na seção crítica, simulando um atraso, ajudando na visualização da concorrência
        if (saldo < valor) {
            System.out.println(Thread.currentThread().getName() + " não conseguiu sacar " + valor + " da conta. Saldo insuficiente. Saldo atual: " + saldo);
        } else {
            lock.lock(); // só entra na seção crítica se realmente puder conseguir sacar
            saldo -= valor;
            lock.unlock(); // saindo da seção crítica ainda dentro do else
            System.out.println(Thread.currentThread().getName() + " sacou " + valor + " da conta. Saldo atual: " + saldo);
        }
    }

    public double getSaldo() {
        return this.saldo;
    }
}

class Cliente {
    private final String nome;
    private final Conta conta;
    private final CountDownLatch latch;

    public Cliente(String nome, Conta conta, CountDownLatch latch) { // inicializa o cliente
        this.nome = nome;
        this.conta = conta;
        this.latch = latch;
    }

    public void depositar(double valor) {
        new Thread(() -> { // criando uma thread
            Thread.currentThread().setName(nome); // nomeando a thread
            try {
                conta.deposito(valor); // chama a função própria da conta
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown(); // decrementa o latch
            }
        }).start();
    }

    public void sacar(double valor) {
        new Thread(() -> { // criando uma thread
            Thread.currentThread().setName(nome); // nomeando a thread
            try {
                conta.saque(valor); // chama a função própria da conta
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown(); // decrementa o latch
            }
        }).start();
    }
}

public class Banco {
    public static void main(String[] args) throws InterruptedException { // throw necessário para usar o await lá no fim
        Conta conta = new Conta(0);
        int numOperacoes = 7; // número de operações pra esse exemplo
        
        // foi necessário criar o latch pra que o programa esperasse todas as operações terminarem até ele printar o saldo final
        CountDownLatch latch = new CountDownLatch(numOperacoes);

        Cliente cliente1 = new Cliente("João", conta, latch);
        Cliente cliente2 = new Cliente("Maria", conta, latch);
        Cliente cliente3 = new Cliente("José", conta, latch);
        Cliente cliente4 = new Cliente("Ana", conta, latch);

        cliente1.sacar(100);
        cliente2.depositar(200);
        cliente3.sacar(1000);
        cliente4.depositar(200);
        cliente1.sacar(300);
        cliente2.depositar(500);
        cliente4.depositar(100);

        latch.await();  // espera todas as operações terminarem pra printar a linha abaixo
        System.out.println("Saldo final: " + conta.getSaldo());
    }
}
