package br.com.tarefa.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.tarefa.dtos.security.AuthenticationRequestDTO;
import br.com.tarefa.dtos.security.AuthenticationTokenDTO;
import br.com.tarefa.services.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    @Operation(summary = "Autenticação do usuário", description = "Autenticação do usuário")
	@ApiResponse(responseCode = "200", description = "Busca realizada com sucesso",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "403", description = "Não autorizado", 
		content = @Content(schema = @Schema(defaultValue = "")))
	public ResponseEntity<AuthenticationTokenDTO> login(@RequestBody AuthenticationRequestDTO dto) {
		UsernamePasswordAuthenticationToken userPasswordToken = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
		this.authManager.authenticate(userPasswordToken);

		return ResponseEntity.ok(jwtService.generateToken(dto));
	}

}
