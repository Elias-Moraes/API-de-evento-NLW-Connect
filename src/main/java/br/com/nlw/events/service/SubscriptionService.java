package br.com.nlw.events.service;

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

        User indicador = userRepo.findById(userId).orElse(null);
        if (indicador == null) {
            throw new UserIndicadorNotFoundException("Usuário" + userId + "indicador não existe.");

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
}
