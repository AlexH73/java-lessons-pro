package de.ait.javalessonspro.controllers.dto;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 21.01.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
public record ValidationErrorResponse(List<String> errors) {
}

