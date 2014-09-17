function avg_dist = curve_avg_distance(C1,C2)
 
% % for what follows we want C1 and C2 to be mx2 arrays
[npts1a npts1b] = size(C1);
[npts2a npts2b] = size(C2);
%  
% if (npts1a == 2)
%     C1 = C1.';
%     npts1a = npts1b;
% end
% if (npts2a == 2)
%     C2 = C2.';
%     npts2a = npts2b;
% end

complong = 0; % Initial value for complong.

% In the next section, we determine the indices of the starting, matched
% points.
if C1(1,1) < C2(1,1)
    j = 1;
    while j <= npts1a
        if C1(j,1) < C2(1,1)
            j = j + 1;
        else 
            complong = 1;
            break;
        end
    end
               
elseif C2(1,1) < C1(1,1)
    j = 1;
    while j <= npts2a
        if C2(j,1) < C1(1,1)
            j = j + 1;
        else 
            complong = 2;
            break;
        end
    end
else
    j = 1;
    complong = 1;
    
end

if npts1a - j < 10 | npts2a - j < 10
    complong = 0;
end

if complong == 0
    %display 'The curves don''t overlap enough (or at all). Setting average distance to 100 in order to start calculation again with different parameters.'
    avg_dist = 100;
else  
    if j > 1
        if complong == 1
            d1 = pdist([C1(j-1,:);C2(1,:)],'euclidean');
            d2 = pdist([C1(j,:);C2(1,:)],'euclidean');
            if npts1a >= j+1
                d3 = pdist([C1(j+1,:);C2(1,:)],'euclidean');
                if d1 <= d2 & d1 <= d3
                    ind = j-1;
                elseif d2 <= d1 & d2 <= d3
                    ind = j;
                else % d3 <= d1 & d3 <= d2
                    ind = j+1;
                end
            else
                if d1 <= d2
                    ind = j-1;
                else
                    ind = j;
                end
            end    
        else
            d1 = pdist([C2(j-1,:);C1(1,:)],'euclidean');
            d2 = pdist([C2(j,:);C1(1,:)],'euclidean');
            if npts2a >= j+1  % Check that C2 has at least j+1 rows.
                d3 = pdist([C2(j+1,:);C1(1,:)],'euclidean');
                if d1 <= d2 & d1 <= d3
                    ind = j-1;
                elseif d2 <= d1 & d2 <= d3
                    ind = j;
                else % d3 <= d1 & d3 <= d2
                    ind = j+1;
                end  
            else
                if d1 <= d2
                    ind = j-1;
                else
                    ind = j;
                end
            end
        end

    else
        d1 = pdist([C1(1,:);C2(1,:)],'euclidean');
        d2 = pdist([C1(1,:);C2(2,:)],'euclidean');
        d3 = pdist([C1(2,:);C2(1,:)],'euclidean');
        if d1 <= d2 & d1 <= d3
                ind = j;
                complong = 1;
        elseif d2 <= d1 & d2 <= d3
                ind = j+1;
                complong = 2;
        else % d3 <= d1 & d3 <= d2
                ind = j+1;
                complong = 1;
        end
    end

    % ind = ind

    % Figure out the number of points that we're going to compare.
    if complong == 1
        npts = min(npts1a-(ind-1),npts2a);
    else
        npts = min(npts2a-(ind-1),npts1a);
    end

    % Cut off one of the curves.
    if complong == 1
        C1 = C1(ind:ind+npts-1,:);
        C2 = C2(1:npts,:);
    else
        C2 = C2(ind:ind+npts-1,:);
        C1 = C1(1:npts,:);
    end

    % Find the average distance between the two curves.  
    dist_sum = 0;

    for k=1:npts
        dist_sum = dist_sum + pdist([C1(k,:);C2(k,:)],'euclidean');
    end

    avg_dist = dist_sum/npts;
end

% norm_est = zeros(npts,1);
% for k=1:npts
%     C2_tmp = circshift(C2,[0 k]); % Don't we want [k,0]?  We don't want to mix up the x and y values.
%     norm_est(k) = norm(C1-C2_tmp(1:npts),2);
% end
%  
% [norm_min k_min] = min(norm_est);





















