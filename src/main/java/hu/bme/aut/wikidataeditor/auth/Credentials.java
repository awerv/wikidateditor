package hu.bme.aut.wikidataeditor.auth;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class Credentials {
	@NotNull
    private String username;
	@NotNull
    private String password;
}