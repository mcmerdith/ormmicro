package net.mcmerdith.ormmicro.testdata.models;

import net.mcmerdith.ormmicro.annotations.*;
import net.mcmerdith.ormmicro.typing.SqlDialect;
import net.mcmerdith.ormmicro.typing.SqlType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestModel1 {
    @Id(autoIncrement = true)
    public Long id;


    public byte pByte;
    public Byte bByte;

    public short pShort;
    public Short bShort;

    public int pInt;
    public Integer bInt;

    public long pLong;
    public Long bLong;

    public float pFloat;
    public Float bFloat;

    public double pDouble;
    public Double bDouble;

    public boolean pBoolean;
    public Boolean bBoolean;

    public BigDecimal bigDecimal1;
    @Column(definition = SqlType.DECIMAL, digits = 25, decimals = 5)
    public BigDecimal bigDecimal2;

    public String string;

    public SqlDialect enum1;
    @EnumStorage(mode = EnumStorage.Mode.ORDINAL)
    public SqlDialect enum2;

    @ForeignKey(externalReference = "foreign(column)")
    public Long externalReferenceId;

    @ForeignKey
    public TestModel2 fk1;
    @ElementCollection
    @ForeignKey
    public List<TestModel2> fk2 = new ArrayList<>();

    @Transient
    public String transientString;

    private TestModel1() {}

    public TestModel1(String string, TestModel2 fk) {
        Random r = new Random();

        this.bByte = this.pByte = (byte) (r.nextInt(Byte.MAX_VALUE + 1) - Byte.MAX_VALUE);
        this.bShort = this.pShort = (short) (r.nextInt(Short.MAX_VALUE + 1) - Short.MAX_VALUE);
        this.bInt = this.pInt = r.nextInt();
        this.externalReferenceId = this.bLong = this.pLong = r.nextLong();
        this.bFloat = this.pFloat = r.nextFloat();
        this.bDouble = this.pDouble = r.nextDouble();
        this.bBoolean = this.pBoolean = r.nextBoolean();
        this.bigDecimal1 = this.bigDecimal2 = BigDecimal.valueOf(r.nextDouble());



        this.string = string;

        this.fk1 = fk;
        this.fk2.add(fk);
    }
}
