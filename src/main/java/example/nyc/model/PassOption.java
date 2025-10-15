package example.nyc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class PassOption {
    private String id;
    private String name;
    private String vendor;
    private int itemCount;
    private int adultPrice;
    private int KidsPrice;
    private String keyBenefit;
    private String purchaseLink;
}