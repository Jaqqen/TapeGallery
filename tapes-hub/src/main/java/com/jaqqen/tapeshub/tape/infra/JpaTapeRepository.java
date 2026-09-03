package com.jaqqen.tapeshub.tape.infra;

import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapeId;
import com.jaqqen.tapeshub.tape.domain.TapeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** The JPA adapter behind {@link TapeRepository}. */
@Repository
class JpaTapeRepository implements TapeRepository {

    private final TapeEntityRepository entities;

    JpaTapeRepository(TapeEntityRepository entities) {
        this.entities = entities;
    }

    @Override
    public List<Tape> findAll() {
        return entities.findAll(Sort.by("title")).stream().map(TapeMapper::toDomain).toList();
    }

    @Override
    public Optional<Tape> findById(TapeId id) {
        return entities.findById(id.value()).map(TapeMapper::toDomain);
    }

    @Override
    public Tape save(Tape tape) {
        entities.save(TapeMapper.toEntity(tape));
        return tape;
    }

    @Override
    public boolean deleteById(TapeId id) {
        if (!entities.existsById(id.value())) {
            return false;
        }
        entities.deleteById(id.value());
        return true;
    }
}
