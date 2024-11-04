package br.com.tarefa.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.tarefa.dtos.security.AuthenticationRequestDTO;
import br.com.tarefa.dtos.security.AuthenticationTokenDTO;
import br.com.tarefa.exceptions.handlers.ApiRequestException;
import br.com.tarefa.services.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Autenticação")
@RestController
@RequestMapping("/auth")
public class AutenticacaoController {
	
	private final JwtService jwtService;
    private final AuthenticationManager authManager;
   
    public AutenticacaoController(JwtService jwtService, AuthenticationManager authManager) {
		this.jwtService = jwtService;
		this.authManager = authManager;
    }
	
	@PostMapping("/login")
    @Operation(summary = "Login do usuário", 
               description = "Login do usuário gera um par de tokens de autenticação "
				    	    +"(access token e refresh token) a partir das informações fornecidas nas credenciais do usuário. "
				    	    +"Este endpoint cria um access token e um refresh token, ambos associados ao usuário autenticado. "
				    		+"O access token é usado para autenticar solicitações subsequentes, enquanto o refresh token "
				    		+"pode ser utilizado para obter um novo access token quando o anterior expirar.")
	@ApiResponse(responseCode = "200", description = "Autenticação com sucesso",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<AuthenticationTokenDTO> login(@RequestBody AuthenticationRequestDTO dto) {
		log.info("Requisição recebida para autenticação do usuário: {}", dto.getUsername());
		UsernamePasswordAuthenticationToken userPasswordToken = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
		this.authManager.authenticate(userPasswordToken);

		return ResponseEntity.ok(jwtService.generateToken(dto));
	}
	
	@PostMapping("/logout")
    @Operation(summary = "Logout do usuário", 
    		   description = "Logout do usuário revoga o token JWT fornecido no cabeçalho de autorização."
	    					+"Este endpoint extrai o token do cabeçalho de autorização e verifica se o token está expirado. "
	    					+"Se o token já tiver expirado, o logout não será realizado. " 
	    					+"Caso contrário, o token é adicionado ao cache lista negra('blacklistedTokens') para "
	    					+"impedir seu uso no futuro para acessar endpoints que exigem autenticação.",
    		   security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponse(responseCode = "204", description = "Logout com sucesso",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Requisição inválida", 
		content = @Content(schema = @Schema(implementation = ApiRequestException.class), 
		mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "401", description = "Não autenticado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		log.info("Requisição recebida para logout do usuário");
		this.jwtService.revokeToken(request.getHeader("Authorization"));
        return ResponseEntity.noContent().build();
	}

}
