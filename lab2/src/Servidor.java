import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashMap;

public class Servidor {

	private static Socket socket;
	private static ServerSocket server;

	private static DataInputStream entrada;
	private static DataOutputStream saida;

	private int porta = 1025;

    public final static Path path = Paths			
			.get("src\\fortune-br.txt");
	private int NUM_FORTUNES = 0;

	public class FileReader {

		public int countFortunes() throws FileNotFoundException {

			int lineCount = 0;

			InputStream is = new BufferedInputStream(new FileInputStream(
					path.toString()));
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					is))) {

				String line = "";
				while (!(line == null)) {

					if (line.equals("%"))
						lineCount++;

					line = br.readLine();

				}// fim while

				System.out.println(lineCount);
			} catch (IOException e) {
				System.out.println("SHOW: Excecao na leitura do arquivo.");
			}
			return lineCount;
		}

		public void parser(HashMap<Integer, String> hm)
				throws FileNotFoundException {

			InputStream is = new BufferedInputStream(new FileInputStream(
					path.toString()));
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					is))) {

				int lineCount = 0;

				String line = "";
				while (!(line == null)) {

					if (line.equals("%"))
						lineCount++;

					line = br.readLine();
					StringBuffer fortune = new StringBuffer();
					while (!(line == null) && !line.equals("%")) {
						fortune.append(line + "\n");
						line = br.readLine();
						// System.out.print(lineCount + ".");
					}

					hm.put(lineCount, fortune.toString());
					System.out.println(fortune.toString());

					System.out.println(lineCount);
				}// fim while

			} catch (IOException e) {
				System.out.println("SHOW: Excecao na leitura do arquivo.");
			}
		}

		public String read(HashMap<Integer, String> hm)
				throws FileNotFoundException {

					SecureRandom random = new SecureRandom();
					int randomIndex = random.nextInt(NUM_FORTUNES) + 1;
				
					String fortune = hm.get(randomIndex);
			
					System.out.println("Fortuna aleatória:");
					System.out.println(fortune);
                    return fortune;
		}

		public void write(HashMap<Integer, String> hm, String nova_fortuna)
				throws IOException {
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toString(), true))) {
						int novaLinha = hm.size() + 1; 
						writer.write("%\n"); 
						writer.write(nova_fortuna + "\n"); 
						hm.put(novaLinha, nova_fortuna); 
						System.out.println("Nova fortuna adicionada com sucesso!");
					} catch (IOException e) {
						System.out.println("SHOW: Excecao na escrita do arquivo.");
						throw e; // Propaga a exceção para o método chamador, se necessário
					}
		}
	}

    public void iniciar() {
        System.out.println("Servidor iniciado na porta: " + porta);
        try {
            FileReader fr = new FileReader();
        try {
            NUM_FORTUNES = fr.countFortunes();
            HashMap hm = new HashMap<Integer, String>();
            fr.parser(hm);
            // Criar porta de recepcao
            server = new ServerSocket(porta);
            socket = server.accept();  //Processo fica bloqueado, ah espera de conexoes

            // Criar os fluxos de entrada e saida
            entrada = new DataInputStream(socket.getInputStream());
            saida = new DataOutputStream(socket.getOutputStream());

            // Recebimento do valor inteiro
            String mensagem = entrada.readUTF();

            // Processamento do valor
            String resultado = "";
            if (mensagem.equals("{\n\"method\":\"read\",\n\"args\":[\"\"]\n}"))
                resultado = fr.read(hm);
            else if (mensagem.equals("{\n\"method\":\"write\",\n\"args\":[\"Mensagem aleatoria\"]\n}")){
                fr.write(hm, "Mensagem aleatória");
                resultado = "Mensagem aleatória.";
            }
    
            // Envio dos dados (resultado)
            saida.writeUTF("{\n\"result\": \"" + resultado + "\n\"\n}");

            socket.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


	public static void main(String[] args) {

		new Servidor().iniciar();

	}

}