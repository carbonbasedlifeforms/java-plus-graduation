package ru.practicum.controller.adminapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;

@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto newCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Admin creating compilation {}", newCompilationDto);

        return compilationService.create(newCompilationDto);

    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long compId) {
        log.info("Admin deleting compilation with id {}", compId);

        compilationService.deleteById(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateById(@PathVariable Long compId,
                                     @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        log.info("Admin updating compilation with id {}", compId);

        return compilationService.updateById(compId, updateCompilationRequest);
    }
}
