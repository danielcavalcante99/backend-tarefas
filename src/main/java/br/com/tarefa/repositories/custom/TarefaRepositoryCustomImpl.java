package br.com.tarefa.repositories.custom;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import br.com.tarefa.dtos.FiltroTarefaDTO;
import br.com.tarefa.entities.Tarefa;
import br.com.tarefa.entities.Tarefa_;
import br.com.tarefa.entities.Usuario;
import br.com.tarefa.entities.Usuario_;

@Repository
public class TarefaRepositoryCustomImpl implements TarefaRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Tarefa> findAllByFilter(FiltroTarefaDTO filtro) {
		CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<Tarefa> query = cb.createQuery(Tarefa.class);
		Root<Tarefa> root = query.from(Tarefa.class);

		query.select(root).where(this.where(cb, root, filtro));
		query.orderBy(cb.desc(root.get(Tarefa_.dataCriacao)));
		return this.entityManager.createQuery(query).getResultList();
	}

	private Predicate[] where(CriteriaBuilder cb, Root<Tarefa> root, FiltroTarefaDTO filtro) {
		List<Predicate> predicates = new ArrayList<>();

		if (filtro == null)
			return null;

		this.expressionEquals(cb, root, predicates, Tarefa_.id, filtro.getId());
		this.expressionEquals(cb, root, predicates, Tarefa_.status, filtro.getStatus());
		this.expressionLikeStart(cb, root, predicates, Tarefa_.titulo, filtro.getTitulo());
		this.expressionLikeStartAndEnd(cb, root, predicates, Tarefa_.descricao, filtro.getDescricao());
		this.expressionEqualsForeignKeyUsuario(cb, root, predicates, Tarefa_.usuario, Usuario_.id, filtro.getUsuarioId());
		this.expressionBetween(cb, root, predicates, Tarefa_.dataAtualizacao, filtro.getDataAtualizacaoInicio(), filtro.getDataAtualizacaoFim());
		this.expressionBetween(cb, root, predicates, Tarefa_.dataCriacao, filtro.getDataCriacaoInicio(), filtro.getDataCriacaoFim());

		return predicates.stream().toArray(Predicate[]::new);
	}

	private void expressionLikeStart(CriteriaBuilder cb, Root<Tarefa> root, List<Predicate> predicates,
			SingularAttribute<Tarefa, String> field, String value) {
		if (StringUtils.isNotEmpty(value))
			predicates.add(cb.like(cb.upper(root.get(field)), value.toUpperCase().concat("%")));
	}

	private void expressionLikeStartAndEnd(CriteriaBuilder cb, Root<Tarefa> root, List<Predicate> predicates,
			SingularAttribute<Tarefa, String> field, String value) {
		if (StringUtils.isNotEmpty(value))
			predicates.add(cb.like(cb.upper(root.get(field)), ("%").concat(value.toUpperCase()).concat("%")));
	}

	private void expressionEquals(CriteriaBuilder cb, Root<Tarefa> root, List<Predicate> predicates,
			SingularAttribute<Tarefa, ?> field, Object value) {
		Optional.ofNullable(value).ifPresent(v -> {
			predicates.add(cb.equal(root.get(field), v));
		});
	}

	private void expressionEqualsForeignKeyUsuario(CriteriaBuilder cb, Root<Tarefa> root, List<Predicate> predicates,
			SingularAttribute<Tarefa, ?> field, SingularAttribute<Usuario, ?> fieldUsuario, Object value) {
		Optional.ofNullable(value).ifPresent(v -> {
			predicates.add(cb.equal(root.get(field).get(fieldUsuario.getName()), v));
		});
	}

	private void expressionBetween(CriteriaBuilder cb, Root<Tarefa> root, List<Predicate> predicates,
			SingularAttribute<Tarefa, LocalDateTime> field, LocalDateTime dateStart, LocalDateTime dateEnd) {
		if(dateStart != null && dateEnd != null)
			predicates.add(cb.between(root.get(field), dateStart, dateEnd));
	}

}
