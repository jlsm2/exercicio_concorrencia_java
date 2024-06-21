class Carro implements Runnable {
    private String direcao;
    private int id;
    private static boolean ponteOcupada = false;

    Carro(String direcao, int id) {
        this.direcao = direcao;
        this.id = id;
    }

    public void run() {
        try {
            atravessar();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void atravessar() throws InterruptedException {
        
        Thread.sleep(1000);
        
        System.out.println("Carro " + id + " vindo da " + direcao + " quer entrar na ponte.");
        
        if (!ponteOcupada) {
            ponteOcupada = true;
            System.out.println("Carro " + id + " vindo da " + direcao + " entrou na ponte.");
            Thread.sleep(1000);
            System.out.println("Carro " + id + " vindo da " + direcao + " saiu da ponte.");
            ponteOcupada = false;
        } else {
            System.out.println("Carro " + id + " vindo da " + direcao + " não conseguiu atravessar.");
        }
    }
}

public class PonteSemControle {
    public static void main(String[] args) {
        for (int i = 1; i <= 5; i++) {
            String direcao = (i % 2 == 0) ? "esquerda" : "direita";
            new Thread(new Carro(direcao, i)).start();
        }
    }
}
