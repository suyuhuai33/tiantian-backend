package edu.hebeu.partnermatching.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDeleteRequest implements Serializable {

    private static final long serialVersionUID = 7468771718037340269L;

    Long id;
}
