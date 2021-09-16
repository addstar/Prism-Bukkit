package me.botsko.prism.serializers.entity;

import me.botsko.prism.utils.MiscUtils;
import org.bukkit.entity.Panda;

public class PandaSerializer extends EntitySerializer<Panda> {
    protected String mainGene = null;
    protected String hiddenGene = null;

    @Override
    public void serialize(Panda entity) {
        super.serialize(entity);
        mainGene = entity.getMainGene().name().toLowerCase();
        hiddenGene = entity.getHiddenGene().name().toLowerCase();
    }

    @Override
    public void deserialize(Panda entity) {
        super.deserialize(entity);
        Panda.Gene mainPandaGene = MiscUtils.getEnum(mainGene, Panda.Gene.NORMAL);
        Panda.Gene hiddenPandaGene = MiscUtils.getEnum(hiddenGene, Panda.Gene.NORMAL);
        entity.setMainGene(mainPandaGene);
        entity.setHiddenGene(hiddenPandaGene);
    }

    @Override
    protected String getPrefix() {
        String niceName = null;
        if (mainGene != null && hiddenGene != null) {
            if ("weak".equals(mainGene) && !"weak".equals(hiddenGene)) {
                niceName = "normal";
            } else if ("brown".equals(mainGene) && !"brown".equals(hiddenGene)) {
                niceName = "normal";
            } else {
                niceName = mainGene;
            }
        }
        return super.getPrefix() + MiscUtils.niceName(niceName);

    }
}
