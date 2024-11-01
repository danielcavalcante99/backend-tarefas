package br.com.tarefa.dtos.security;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationTokenDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("expires_in_ml")
	private Long expiresInMl;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("refresh_expires_token_ml")
	private Long refreshExpiresInMl;

}