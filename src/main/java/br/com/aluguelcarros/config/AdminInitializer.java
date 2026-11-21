package br.com.aluguelcarros.config;

import br.com.aluguelcarros.entity.PerfilUsuario;
import br.com.aluguelcarros.entity.Usuario;
import br.com.aluguelcarros.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner criarAdministradorInicial(
         UsuarioRepository usuarioRepository,
         PasswordEncoder passwordEncoder,
         @Value("${app.admin.nome}") String nome,
         @Value("${app.admin.email}") String email,
         @Value("${app.admin.senha}") String senha) {

        return args -> {

            if (usuarioRepository.findByEmail(email).isPresent()) {
                return;
            }

            Usuario administrador = new Usuario();

            administrador.setNome(nome);
            administrador.setEmail(email);
            administrador.setSenha(passwordEncoder.encode(senha));
            administrador.setPerfil(PerfilUsuario.ADMIN);
            administrador.setAtivo(true);

            usuarioRepository.save(administrador);

        };
    }
}
