package ar.com.controlfinanzas.service;

import java.util.List;

import ar.com.controlfinanzas.model.Usuario;
import ar.com.controlfinanzas.repository.UsuarioRepository;

public class UsuarioService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioService() {
		this.usuarioRepository = new UsuarioRepository();
	}

	public void guardarUsuario(Usuario usuario) {
		usuarioRepository.guardar(usuario);
	}

	public Usuario obtenerUsuario(Integer id) {
		return usuarioRepository.buscarPorId(id);
	}

	public List<Usuario> listarUsuarios() {
		return usuarioRepository.listarTodos();
	}

	public void eliminarUsuario(Usuario usuario) {
		usuarioRepository.eliminar(usuario);
	}
}
