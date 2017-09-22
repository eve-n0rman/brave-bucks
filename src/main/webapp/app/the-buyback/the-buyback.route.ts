import { Route } from '@angular/router';

import { TheBuybackComponent } from './';

export const TheBuyback_ROUTE: Route = {
    path: 'buyback',
    component: TheBuybackComponent,
    data: {
        authorities: [],
        pageTitle: 'The Buyback'
    }
};
