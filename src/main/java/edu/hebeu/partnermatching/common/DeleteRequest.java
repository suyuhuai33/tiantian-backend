package edu.hebeu.partnermatching.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = -7059765005727213368L;

    /**
     * id
     */
    private Long id;

}
