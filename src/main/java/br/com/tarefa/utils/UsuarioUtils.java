package br.com.tarefa.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioUtils {

	public static String getUsuarioLogado() {
		final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String userLogado;

		if (principal instanceof UserDetails) {
			userLogado = ((UserDetails) principal).getUsername();
		} else {
			userLogado = principal.toString();
		}

		return userLogado;
	}

}
