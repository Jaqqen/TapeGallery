package com.jaqqen.tapeshub.tape.infra;

import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapeRepository;
import com.jaqqen.tapeshub.tape.domain.exception.TapeNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaTapeEntityRepository implements TapeRepository {
    private final TapeEntityRepository ter;
    private final TapeMapper tapeMapper;

    public JpaTapeEntityRepository(TapeEntityRepository ter, TapeMapper tapeMapper) {
        this.ter = ter;
        this.tapeMapper = tapeMapper;
    }

    @Override
    public List<Tape> findAll() {
        return ter.findAll(Sort.by("title")).stream()
            .map(tapeMapper::toDomain)
            .toList();
    }

    @Override
    public Tape save(Tape tape) {
        ter.save(tapeMapper.toEntity(tape));
        return tape;
    }

    @Override
    public Tape findById(UUID id) throws TapeNotFoundException {
        return ter.findById(id)
            .map(tapeMapper::toDomain)
            .orElseThrow(() -> new TapeNotFoundException(id));

    }

    @Override
    public boolean existsById(UUID id) {
        return ter.existsById(id);
    }

    @Override
    public boolean deleteById(UUID id) {
        if (!ter.existsById(id)) {
            return false;
        }
        ter.deleteById(id);
        return true;
    }
}
