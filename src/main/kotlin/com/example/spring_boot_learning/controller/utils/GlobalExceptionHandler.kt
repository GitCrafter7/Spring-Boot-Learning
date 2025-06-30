package com.example.spring_boot_learning.controller.utils
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ErrorHandlerController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): Errors = Errors(
        errors = ex.bindingResult
            .fieldErrors
            .map {
                Error(
                    key = it.field,
                    message = it.defaultMessage ?: "Validation error"
                )
            }
    )

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): Errors = Errors(
        errors = listOf(
            Error(
                key = ex.javaClass.simpleName,
                message = ex.message ?: "unknown error"
            )
        )
    )
}

data class Errors(val errors: List<Error>)

data class Error(
    val key: String,
    val message: String,
)