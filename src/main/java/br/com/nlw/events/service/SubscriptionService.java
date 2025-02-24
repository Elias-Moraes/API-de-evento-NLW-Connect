package br.com.nlw.events.service;

import br.com.nlw.events.dto.SubscriptionRankingByUser;
import br.com.nlw.events.dto.SubscriptionRankingItem;
import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exception.EventNotFoundException;
import br.com.nlw.events.exception.SubscriptionConflictException;
import br.com.nlw.events.exception.UserIndicadorNotFoundException;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.repo.EventRepo;
import br.com.nlw.events.repo.SubscriptionRepo;
import br.com.nlw.events.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.com.nlw.events.model.User;
import br.com.nlw.events.model.Subscription;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class SubscriptionService {

    @Autowired
    private EventRepo evtRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SubscriptionRepo subRepo;

    public SubscriptionResponse createNewSubsrcription(String eventName, User user, Integer userId) {

        // recuperar o evento pelo nome.
        //Caso alternativo 2.
        Event evt = evtRepo.findByPrettyName(eventName);
        if (evt == null) {
            throw new EventNotFoundException("Evento" + "não existe.");

        }

        //Caso alternativo 1.
        User userRec = userRepo.findByEmail(user.getEmail());
        if (userRec == null) {
            userRec = userRepo.save(user);
        }

        User indicador = null;
        if (userId != null) {
            indicador = userRepo.findById(userId).orElse(null);

            if (indicador == null) {
                throw new UserIndicadorNotFoundException("Usuário" + userId + "indicador não existe.");

            }

        }


        Subscription subs = new Subscription();
        subs.setEvent(evt);
        subs.setSubscriber(userRec);
        subs.setIndication(indicador);

        //Caso alternativo 3.
        Subscription tmpSub = subRepo.findByEventAndSubscriber(evt, userRec);
        if (tmpSub != null) {

            throw new SubscriptionConflictException("Já existe incrição para o usuário" + userRec.getName());
        }
        Subscription resultado = subRepo.save(subs);
        return new SubscriptionResponse(resultado.getSubscriptionNumber(), "http://codecraft.com/"
                + resultado.getEvent().getPrettyName() + "/" + resultado.getSubscriber().getId());

    }

    public List<SubscriptionRankingItem> getCompleteRanking(String prettyName) {
        Event evt = evtRepo.findByPrettyName(prettyName);
        if (evt == null) {

            throw new EventNotFoundException("Ranking do evento " + prettyName + " não existe");

        }

        return subRepo.generateRanking(evt.getEventId());
    }

    public SubscriptionRankingByUser getRankingByUser(String prettyName, Integer userId) {

        List<SubscriptionRankingItem> ranking = getCompleteRanking(prettyName);

        SubscriptionRankingItem item = ranking.stream().filter(i -> i.userId().equals(userId)).findFirst().orElse(null);

        if (item == null) {
            throw new UserIndicadorNotFoundException("Não há inscrições com indicação do usuário " + userId);
        }

        Integer posicao = IntStream.range(0, ranking.size())
                .filter(pos -> ranking.get(pos).userId().equals(userId))
                .findFirst().getAsInt();

        return new SubscriptionRankingByUser(item, posicao + 1);

    }
}
