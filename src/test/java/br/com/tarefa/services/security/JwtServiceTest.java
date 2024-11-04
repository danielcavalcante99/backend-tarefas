package br.com.tarefa.services.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import br.com.tarefa.dtos.security.AuthenticationRequestDTO;
import br.com.tarefa.dtos.security.AuthenticationTokenDTO;
import br.com.tarefa.exceptions.RevokeTokenException;

class JwtServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

	@Value("${application.security.jwt.expiration}")
	private long accessTokenExpiration;
	
	@Value("${application.security.jwt.refresh-token.expiration}")
	private long tokenRefreshExpiration;

    private JwtService jwtService;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
        this.jwtService = new JwtService(cacheManager);
        when(cacheManager.getCache("blacklistedTokens")).thenReturn(cache);
    }

    @Test
    public void testeRevogandoToken() {
    	String accesstoken = RandomStringUtils.randomAlphanumeric(135);
    	String authHeader = "Bearer ".concat(accesstoken);
       
    	// Mockando o comportamento do cache
        when(cache.get(accesstoken, String.class)).thenReturn(null); // O token não está na blacklist
        
        this.jwtService = spy(this.jwtService);
        doReturn(false).when(this.jwtService).isTokenExpired(accesstoken); // O token não está na expirado
        this.jwtService.revokeToken(authHeader);

        // Capturando o token que foi colocado no cache
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(cache).putIfAbsent(captor.capture(), eq(accesstoken));
        
        // Verificando se o token foi corretamente colocado no cache
        assertEquals(accesstoken, captor.getValue());
    }

    @Test
    public void testeTentarRevogarTokenJaExpirado() {
        String tokenExpirado = RandomStringUtils.randomAlphanumeric(135);
        String authHeader = "Bearer ".concat(tokenExpirado);

        // Mockando o comportamento do cache
        when(this.cache.get(tokenExpirado, String.class)).thenReturn(null);
        
        this.jwtService = spy(this.jwtService);
        doReturn(true).when(this.jwtService).isTokenExpired(tokenExpirado);// Simulando que o token está expirado   

        RevokeTokenException exception = assertThrows(RevokeTokenException.class, () -> {
            this.jwtService.revokeToken(authHeader);
        });

        assertEquals("Não foi possível revogar o token JWT durante o logout, pois ele já está expirado", exception.getMessage());
    }

    @Test
    public void testeGerarTokenComSucesso() {
        AuthenticationRequestDTO requestDTO = new AuthenticationRequestDTO();
        requestDTO.setUsername("testUser");
        requestDTO.setPassword("123456789");
        
        String accesstoken = RandomStringUtils.randomAlphanumeric(135);
        String refreshToken = RandomStringUtils.randomAlphanumeric(135);
        
        this.jwtService = spy(this.jwtService);
        doReturn(accesstoken).when(this.jwtService).generateToken(new HashMap<>(), requestDTO); 
        doReturn(refreshToken).when(this.jwtService).generateRefreshToken(requestDTO); 
        
        // Chamando o método para gerar o token
        AuthenticationTokenDTO tokenDTO = this.jwtService.generateToken(requestDTO);
        
        assertEquals(tokenDTO.getAccessToken(), accesstoken);
        assertEquals(tokenDTO.getRefreshToken(), refreshToken);
        assertEquals(this.accessTokenExpiration, tokenDTO.getExpiresInMl());
        assertEquals(this.tokenRefreshExpiration, tokenDTO.getRefreshExpiresInMl());
    }

}
