package com.example.gestionsolicitudes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionsolicitudes.data.repository.PostRepository
import com.example.gestionsolicitudes.model.PostUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** El repositorio se recibe por constructor (lo entrega ViewModelFactory o un mock en las pruebas). */
class PostViewModel(
    private val repositorio: PostRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(PostUiState())
    val estado: StateFlow<PostUiState> = _estado

    init {
        cargarPosts()
    }

    fun cargarPosts() {
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val posts = repositorio.getPosts()
                _estado.update { it.copy(posts = posts, cargando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update {
                    it.copy(cargando = false, error = "No se pudieron cargar los datos. Revisa tu conexión.")
                }
            }
        }
    }
}
