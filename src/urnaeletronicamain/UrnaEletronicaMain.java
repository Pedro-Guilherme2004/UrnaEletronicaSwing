/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package urnaeletronicamain;



import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UrnaEletronicaMain {

    // Componentes de tela
    JLabel cargoLabel, numeroLabel, nomeLabel, partidoLabel, mensagemLabel, digitosLabel;
    JLabel fotoLabel;
    JButton[] botoesNumericos;
    JButton botaoBranco, botaoCorrige, botaoConfirma, botaoResultado;

    // Fontes
    Font fontTitulo, fontNormal, fontDigitos;

    // Lógica de votação
    String numeroDigitado = "";
    boolean votoBrancoSelecionado = false;

    // Estrutura de cargos
    static class Candidato {
        String numero;
        String nome;
        String partido;
        String caminhoFoto;
        int votos;

        public Candidato(String numero, String nome, String partido, String caminhoFoto) {
            this.numero = numero;
            this.nome = nome;
            this.partido = partido;
            this.caminhoFoto = caminhoFoto;
            this.votos = 0;
        }
    }

    static class Cargo {
        String nome;
        int digitos;
        Map<String, Candidato> candidatos = new HashMap<>();
        int votosBranco = 0;
        int votosNulo = 0;

        Cargo(String nome, int digitos) {
            this.nome = nome;
            this.digitos = digitos;
        }
    }

    List<Cargo> cargos = new ArrayList<>();
    int cargoAtualIndex = 0;

    BotaoHandler bHandler = new BotaoHandler();

    public static void main(String[] args) {
        new UrnaEletronicaMain();
    }

    public UrnaEletronicaMain() {
        criarFontes();
        criarCargosECandidatos();
        criarUI();
        atualizarCargoNaTela();
    }

    private void criarFontes() {
        fontTitulo = new Font("Comic Sans MS", Font.BOLD, 24);
        fontNormal = new Font("Comic Sans MS", Font.PLAIN, 16);
        fontDigitos = new Font("Comic Sans MS", Font.BOLD, 32);
    }

    // ==== 100 candidatos (20 por cargo) usando foto padrão ====
    private void criarCargosECandidatos() {

        String caminhoFotoPadrao = "/fotos/candidato_default.png";

        // Deputado Estadual (5 dígitos) - 20 candidatos: 10000..10019
        Cargo depEst = new Cargo("DEPUTADO ESTADUAL", 5);
        for (int i = 0; i < 20; i++) {
            String numero = String.format("%05d", 10000 + i);
            String nome = "CAND ESTADUAL " + (i + 1);
            String partido = "PE" + String.format("%02d", (i % 10) + 1);
            depEst.candidatos.put(
                    numero,
                    new Candidato(numero, nome, partido, caminhoFotoPadrao)
            );
        }

        // Deputado Federal (4 dígitos) - 20 candidatos: 2000..2019
        Cargo depFed = new Cargo("DEPUTADO FEDERAL", 4);
        for (int i = 0; i < 20; i++) {
            String numero = String.format("%04d", 2000 + i);
            String nome = "CAND FEDERAL " + (i + 1);
            String partido = "PF" + String.format("%02d", (i % 10) + 1);
            depFed.candidatos.put(
                    numero,
                    new Candidato(numero, nome, partido, caminhoFotoPadrao)
            );
        }

        // Senador (3 dígitos) - 20 candidatos: 300..319
        Cargo senador = new Cargo("SENADOR", 3);
        for (int i = 0; i < 20; i++) {
            String numero = String.format("%03d", 300 + i);
            String nome = "CAND SENADOR " + (i + 1);
            String partido = "PS" + String.format("%02d", (i % 10) + 1);
            senador.candidatos.put(
                    numero,
                    new Candidato(numero, nome, partido, caminhoFotoPadrao)
            );
        }

        // Governador (2 dígitos) - 20 candidatos: 40..59
        Cargo governador = new Cargo("GOVERNADOR", 2);
        for (int i = 0; i < 20; i++) {
            String numero = String.format("%02d", 40 + i);
            String nome = "CAND GOVERNADOR " + (i + 1);
            String partido = "PG" + String.format("%02d", (i % 10) + 1);
            governador.candidatos.put(
                    numero,
                    new Candidato(numero, nome, partido, caminhoFotoPadrao)
            );
        }

        // Presidente (2 dígitos) - 20 candidatos: 60..79
        Cargo presidente = new Cargo("PRESIDENTE", 2);
        for (int i = 0; i < 20; i++) {
            String numero = String.format("%02d", 60 + i);
            String nome = "CAND PRESIDENTE " + (i + 1);
            String partido = "PP" + String.format("%02d", (i % 10) + 1);
            presidente.candidatos.put(
                    numero,
                    new Candidato(numero, nome, partido, caminhoFotoPadrao)
            );
        }

        cargos.add(depEst);
        cargos.add(depFed);
        cargos.add(senador);
        cargos.add(governador);
        cargos.add(presidente);
    }

    private Cargo getCargoAtual() {
        return cargos.get(cargoAtualIndex);
    }

    // ==== INTERFACE GRÁFICA ====
    private void criarUI() {
        JFrame window = new JFrame("Urna Eletrônica - Simulação");
        window.setSize(900, 620);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().setBackground(new Color(30, 30, 30));
        window.setLayout(null);
        window.setResizable(false);

        JPanel urnaPanel = new JPanel();
        urnaPanel.setBounds(20, 20, 840, 540);
        urnaPanel.setBackground(new Color(200, 200, 200));
        urnaPanel.setLayout(null);
        window.add(urnaPanel);

        // Tela
        JPanel telaPanel = new JPanel();
        telaPanel.setBounds(20, 20, 500, 260);
        telaPanel.setBackground(Color.black);
        telaPanel.setLayout(null);
        urnaPanel.add(telaPanel);

        JPanel infoPanel = new JPanel();
        infoPanel.setBounds(10, 10, 320, 150);
        infoPanel.setBackground(Color.black);
        infoPanel.setLayout(null);
        telaPanel.add(infoPanel);

        cargoLabel = new JLabel();
        cargoLabel.setForeground(Color.white);
        cargoLabel.setFont(fontTitulo);
        cargoLabel.setBounds(10, 5, 300, 30);
        infoPanel.add(cargoLabel);

        JLabel labelNumero = new JLabel("Número:");
        labelNumero.setForeground(Color.white);
        labelNumero.setFont(fontNormal);
        labelNumero.setBounds(10, 50, 100, 25);
        infoPanel.add(labelNumero);

        numeroLabel = new JLabel("");
        numeroLabel.setForeground(Color.white);
        numeroLabel.setFont(fontNormal);
        numeroLabel.setBounds(110, 50, 200, 25);
        infoPanel.add(numeroLabel);

        JLabel labelNome = new JLabel("Nome:");
        labelNome.setForeground(Color.white);
        labelNome.setFont(fontNormal);
        labelNome.setBounds(10, 80, 100, 25);
        infoPanel.add(labelNome);

        nomeLabel = new JLabel("");
        nomeLabel.setForeground(Color.white);
        nomeLabel.setFont(fontNormal);
        nomeLabel.setBounds(110, 80, 200, 25);
        infoPanel.add(nomeLabel);

        JLabel labelPartido = new JLabel("Partido:");
        labelPartido.setForeground(Color.white);
        labelPartido.setFont(fontNormal);
        labelPartido.setBounds(10, 110, 100, 25);
        infoPanel.add(labelPartido);

        partidoLabel = new JLabel("");
        partidoLabel.setForeground(Color.white);
        partidoLabel.setFont(fontNormal);
        partidoLabel.setBounds(110, 110, 200, 25);
        infoPanel.add(partidoLabel);

        // Foto do candidato
        fotoLabel = new JLabel();
        fotoLabel.setBounds(340, 20, 140, 140);
        telaPanel.add(fotoLabel);

        // Mensagem
        JPanel mensagemPanel = new JPanel();
        mensagemPanel.setBounds(10, 170, 480, 70);
        mensagemPanel.setBackground(Color.black);
        mensagemPanel.setLayout(null);
        telaPanel.add(mensagemPanel);

        mensagemLabel = new JLabel("Digite o número do seu candidato e aperte CONFIRMA.");
        mensagemLabel.setForeground(Color.white);
        mensagemLabel.setFont(fontNormal);
        mensagemLabel.setBounds(10, 10, 460, 50);
        mensagemPanel.add(mensagemLabel);

        // Dígitos grandes
        JPanel digitosPanel = new JPanel();
        digitosPanel.setBounds(20, 290, 200, 80);
        digitosPanel.setBackground(Color.black);
        digitosPanel.setLayout(null);
        urnaPanel.add(digitosPanel);

        digitosLabel = new JLabel("");
        digitosLabel.setForeground(Color.white);
        digitosLabel.setFont(fontDigitos);
        digitosLabel.setBounds(10, 10, 180, 60);
        digitosPanel.add(digitosLabel);

        // Teclado numérico
        JPanel tecladoPanel = new JPanel();
        tecladoPanel.setBounds(560, 80, 240, 220);
        tecladoPanel.setBackground(new Color(60, 60, 60));
        tecladoPanel.setLayout(new GridLayout(4, 3, 5, 5));
        urnaPanel.add(tecladoPanel);

        botoesNumericos = new JButton[10];

        for (int i = 1; i <= 9; i++) {
            botoesNumericos[i] = new JButton(String.valueOf(i));
            botoesNumericos[i].setFont(fontNormal);
            botoesNumericos[i].setFocusPainted(false);
            botoesNumericos[i].setBackground(new Color(220, 220, 220));
            botoesNumericos[i].addActionListener(bHandler);
            botoesNumericos[i].setActionCommand(String.valueOf(i));
            tecladoPanel.add(botoesNumericos[i]);
        }

        JButton vazio = new JButton("");
        vazio.setEnabled(false);
        tecladoPanel.add(vazio);

        botoesNumericos[0] = new JButton("0");
        botoesNumericos[0].setFont(fontNormal);
        botoesNumericos[0].setFocusPainted(false);
        botoesNumericos[0].setBackground(new Color(220, 220, 220));
        botoesNumericos[0].addActionListener(bHandler);
        botoesNumericos[0].setActionCommand("0");
        tecladoPanel.add(botoesNumericos[0]);

        JButton vazio2 = new JButton("");
        vazio2.setEnabled(false);
        tecladoPanel.add(vazio2);

        // Botões de ação
        JPanel acoesPanel = new JPanel();
        acoesPanel.setBounds(560, 320, 240, 200);
        acoesPanel.setBackground(new Color(60, 60, 60));
        acoesPanel.setLayout(new GridLayout(2, 2, 10, 10));
        urnaPanel.add(acoesPanel);

        botaoBranco = new JButton("BRANCO");
        botaoBranco.setFont(fontNormal);
        botaoBranco.setFocusPainted(false);
        botaoBranco.setBackground(Color.WHITE);
        botaoBranco.setForeground(Color.BLACK);
        botaoBranco.addActionListener(bHandler);
        botaoBranco.setActionCommand("BRANCO");
        acoesPanel.add(botaoBranco);

        botaoCorrige = new JButton("CORRIGE");
        botaoCorrige.setFont(fontNormal);
        botaoCorrige.setFocusPainted(false);
        botaoCorrige.setBackground(new Color(255, 153, 51));
        botaoCorrige.setForeground(Color.BLACK);
        botaoCorrige.addActionListener(bHandler);
        botaoCorrige.setActionCommand("CORRIGE");
        acoesPanel.add(botaoCorrige);

        botaoConfirma = new JButton("CONFIRMA");
        botaoConfirma.setFont(fontNormal);
        botaoConfirma.setFocusPainted(false);
        botaoConfirma.setBackground(new Color(0, 153, 0));
        botaoConfirma.setForeground(Color.BLACK);
        botaoConfirma.addActionListener(bHandler);
        botaoConfirma.setActionCommand("CONFIRMA");
        acoesPanel.add(botaoConfirma);

        botaoResultado = new JButton("RESULTADO");
        botaoResultado.setFont(fontNormal);
        botaoResultado.setFocusPainted(false);
        botaoResultado.setBackground(new Color(102, 178, 255));
        botaoResultado.setForeground(Color.BLACK);
        botaoResultado.addActionListener(bHandler);
        botaoResultado.setActionCommand("RESULTADO");
        acoesPanel.add(botaoResultado);

        window.setVisible(true);
    }

    // ===== LÓGICA =====

    private void atualizarCargoNaTela() {
        Cargo cargo = getCargoAtual();
        cargoLabel.setText("ELEIÇÃO - " + cargo.nome);
        corrigir(); // limpa tudo para o novo cargo
    }

    private void adicionarDigito(String d) {
        if (votoBrancoSelecionado) {
            mensagemLabel.setText("Você selecionou BRANCO. Use CORRIGE para mudar.");
            return;
        }

        Cargo cargoAtual = getCargoAtual();

        if (numeroDigitado.length() >= cargoAtual.digitos) {
            return;
        }

        numeroDigitado += d;
        digitosLabel.setText(numeroDigitado);
        numeroLabel.setText(numeroDigitado);

        if (numeroDigitado.length() == cargoAtual.digitos) {
            atualizarInfoCandidato(numeroDigitado);
        } else {
            limparInfoCandidato();
        }
    }

    private void atualizarInfoCandidato(String numero) {
        Cargo cargoAtual = getCargoAtual();
        Candidato c = cargoAtual.candidatos.get(numero);

        if (c != null) {
            nomeLabel.setText(c.nome);
            partidoLabel.setText(c.partido);
            mensagemLabel.setText("Confirme seu voto ou aperte CORRIGE para alterar.");
            atualizarFoto(c.caminhoFoto);
        } else {
            nomeLabel.setText("VOTO NULO");
            partidoLabel.setText("");
            mensagemLabel.setText("Número inexistente. Se confirmar será VOTO NULO.");
            limparFoto();
        }
    }

    private void limparInfoCandidato() {
        nomeLabel.setText("");
        partidoLabel.setText("");
        limparFoto();
    }

    // ==== FOTO DO CANDIDATO ====
    private void atualizarFoto(String caminhoFoto) {
        if (caminhoFoto == null || caminhoFoto.isEmpty()) {
            limparFoto();
            return;
        }
        try {
            java.net.URL imgUrl = getClass().getResource(caminhoFoto);
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image img = icon.getImage().getScaledInstance(
                        fotoLabel.getWidth(), fotoLabel.getHeight(), Image.SCALE_SMOOTH);
                fotoLabel.setIcon(new ImageIcon(img));
            } else {
                limparFoto();
            }
        } catch (Exception e) {
            e.printStackTrace();
            limparFoto();
        }
    }

    private void limparFoto() {
        fotoLabel.setIcon(null);
    }

    private void selecionarBranco() {
        limparDigitacao();
        votoBrancoSelecionado = true;
        Cargo cargoAtual = getCargoAtual();
        mensagemLabel.setText("VOTO EM BRANCO para " + cargoAtual.nome +
                ". Aperte CONFIRMA para confirmar ou CORRIGE para voltar.");
        nomeLabel.setText("VOTO EM BRANCO");
        partidoLabel.setText("");
        numeroLabel.setText("");
        limparFoto();
    }

    private void corrigir() {
        limparDigitacao();
        votoBrancoSelecionado = false;
        mensagemLabel.setText("Digite o número do seu candidato e aperte CONFIRMA.");
    }

    private void confirmar() {
        Cargo cargoAtual = getCargoAtual();

        if (votoBrancoSelecionado) {
            cargoAtual.votosBranco++;
            mensagemLabel.setText("VOTO EM BRANCO COMPUTADO para " + cargoAtual.nome + ".");
        } else if (numeroDigitado.length() == cargoAtual.digitos) {
            Candidato c = cargoAtual.candidatos.get(numeroDigitado);
            if (c != null) {
                c.votos++;
                mensagemLabel.setText("VOTO EM " + c.nome + " COMPUTADO para " + cargoAtual.nome + ".");
            } else {
                cargoAtual.votosNulo++;
                mensagemLabel.setText("VOTO NULO COMPUTADO para " + cargoAtual.nome + ".");
            }
        } else {
            cargoAtual.votosNulo++;
            mensagemLabel.setText("Número incompleto. VOTO NULO COMPUTADO para " + cargoAtual.nome + ".");
        }

        tocarSomConfirmacao();
        avancarParaProximoCargo();
    }

    private void avancarParaProximoCargo() {
        limparDigitacao();
        votoBrancoSelecionado = false;

        cargoAtualIndex++;

        if (cargoAtualIndex >= cargos.size()) {
            cargoAtualIndex = 0;
            mensagemLabel.setText("FIM DA VOTAÇÃO. Iniciando NOVO ELEITOR para " +
                    getCargoAtual().nome + ".");
        }

        atualizarCargoNaTela();
    }

    private void limparDigitacao() {
        numeroDigitado = "";
        digitosLabel.setText("");
        numeroLabel.setText("");
        nomeLabel.setText("");
        partidoLabel.setText("");
        limparFoto();
    }

    // ===== RELATÓRIO TXT/CSV =====

    private String gerarStringRelatorioTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("RESULTADO DA VOTAÇÃO\n");
        sb.append("================================\n\n");

        for (Cargo cargo : cargos) {
            sb.append("== ").append(cargo.nome).append(" ==\n");

            int totalValidos = 0;
            for (Candidato c : cargo.candidatos.values()) {
                totalValidos += c.votos;
            }
            int totalBrancos = cargo.votosBranco;
            int totalNulos   = cargo.votosNulo;
            int totalGeral   = totalValidos + totalBrancos + totalNulos;

            sb.append(String.format("Total de votos (cargo): %d%n", totalGeral));
            sb.append(String.format("Válidos: %d   Brancos: %d   Nulos: %d%n%n",
                    totalValidos, totalBrancos, totalNulos));

            for (Candidato c : cargo.candidatos.values()) {
                double perc = (totalGeral > 0) ? (c.votos * 100.0 / totalGeral) : 0.0;

                sb.append(String.format(
                        "Nº %s - %-20s  Partido: %-8s  Votos: %-4d (%.2f%%)%n",
                        c.numero, c.nome, c.partido, c.votos, perc
                ));
            }

            sb.append("\n");
        }
        return sb.toString();
    }

    private String gerarStringRelatorioCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cargo;Numero;Nome;Partido;Votos;Percentual;Tipo\n");

        for (Cargo cargo : cargos) {

            int totalValidos = 0;
            for (Candidato c : cargo.candidatos.values()) {
                totalValidos += c.votos;
            }
            int totalBrancos = cargo.votosBranco;
            int totalNulos   = cargo.votosNulo;
            int totalGeral   = totalValidos + totalBrancos + totalNulos;

            for (Candidato c : cargo.candidatos.values()) {
                double perc = (totalGeral > 0) ? (c.votos * 100.0 / totalGeral) : 0.0;
                sb.append(String.format(
                        "%s;%s;%s;%s;%d;%.2f;CANDIDATO%n",
                        cargo.nome, c.numero, c.nome, c.partido, c.votos, perc
                ));
            }

            double percBrancos = (totalGeral > 0) ? (totalBrancos * 100.0 / totalGeral) : 0.0;
            double percNulos   = (totalGeral > 0) ? (totalNulos   * 100.0 / totalGeral) : 0.0;

            sb.append(String.format(
                    "%s;;BRANCOS;;%d;%.2f;BRANCO%n",
                    cargo.nome, totalBrancos, percBrancos
            ));
            sb.append(String.format(
                    "%s;;NULOS;;%d;%.2f;NULO%n",
                    cargo.nome, totalNulos, percNulos
            ));
        }

        return sb.toString();
    }

    private void salvarRelatoriosEmArquivo() {
        String texto = gerarStringRelatorioTexto();
        String csv   = gerarStringRelatorioCSV();

        try (PrintWriter outTxt = new PrintWriter(new FileWriter("resultado_votacao.txt"))) {
            outTxt.print(texto);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter outCsv = new PrintWriter(new FileWriter("resultado_votacao.csv"))) {
            outCsv.print(csv);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mensagemLabel.setText("Relatórios gerados: resultado_votacao.txt e resultado_votacao.csv");
    }

    private void mostrarResultadoGeral() {
        salvarRelatoriosEmArquivo();
    }

    // ===== SOM DE CONFIRMAÇÃO =====

    private void tocarSomConfirmacao() {
        try {
            java.net.URL soundUrl = getClass().getResource("/som_confirmacao.wav");
            System.out.println("URL do som: " + soundUrl);

            if (soundUrl == null) {
                System.err.println("Arquivo de som não encontrado no classpath.");
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handler dos botões
    public class BotaoHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String acao = e.getActionCommand();

            switch (acao) {
                case "0": case "1": case "2": case "3": case "4":
                case "5": case "6": case "7": case "8": case "9":
                    adicionarDigito(acao);
                    break;
                case "BRANCO":
                    selecionarBranco();
                    break;
                case "CORRIGE":
                    corrigir();
                    break;
                case "CONFIRMA":
                    confirmar();
                    break;
                case "RESULTADO":
                    mostrarResultadoGeral();
                    break;
            }
        }
    }
}