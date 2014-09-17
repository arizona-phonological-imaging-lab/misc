function best_match = tongue_matcher2(C1,C2,theta,xs,ys,fval_prev)

if nargin < 7
    fval_prev = 100;
end
% f = transform_tongue(x,c1,c2)
% Note that this function leaves C1 the same and transforms C2 to be closer
% to C1.  
c1 = C1;
c2 = C2;

% Tweak the input values with a bit of randomization.
randxs = (rand-0.5)*10; % Generates random numbers between -5 and 5.
randys = (rand-0.5)*6; % Generates random numbers between -3 and 3.
randtheta = (rand-0.5)*40;

[x, fval] = fminsearch(@(x) transform_tongue(x,c1,c2),[theta+pi/randtheta,xs+randxs,ys+randys],optimset('TolFun',1e-8));


%EDIT 8/16/14:SAM J
%I increased this value under the impression it was only intended to limit
%the amount of functions to try, and that it does not affect the accuracy
%of the end result.  This is to allow for more attempts, leading to a
%quicker run-time and more available data.
if fval < 18;   % I CHANGED THIS IN ORDER TO TEST THE REST OF MY PROGRAM.
    % Now, apply the best rotation and translation to C2.
    % Rotate C2:
    rotmat = [cos(x(1)),-sin(x(1));sin(x(1)),cos(x(1))];
    C2_temp = rotmat*C2';

    % Translate C2:
    [r,c] = size(C2_temp);
    transmat = repmat([x(2);x(3)],1,c);
    new_C2 = (C2_temp + transmat)';
%     display C1
    
    plot(C1(:,1),-C1(:,2),'b','LineWidth',4);
        hold on;
    plot(C2(:,1),-C2(:,2),'-g', 'LineWidth',2);
    plot(new_C2(:,1),-new_C2(:,2),'--r','LineWidth',1.5);
    legend('Curve 1', 'Curve 2', 'New Curve 2')
    title('Neutral Tongue Curves:  Original & Transformed')
        hold off;
    %EDIT 5-6-14: Sam Johnston
    %the user input is replaced with an autoreply, to allow for mass trans.
    %userok = input('Is this fit good enough?  If yes, type ''Y'' (with quotes).\nIf not, type ''N'' (with quotes). If you choose ''N'',\nyou''ll get a chance to start the process again.  Response: ')
    
    %autoreply to always use the program generated contour
    userok = 'Y';
    
    if userok == 'Y' | userok == 'y'
        best_match = x;
    else
        % Return to beginning.  Figue out how to do this.
        display 'Okay.  Try some different initial values for rotation and translation.'
        xs = input('How much does x need to be shifted by to move Curve 2 onto Curve 1?\nSample responses: 15, -20 (no quotes).  Response: ')
        ys = input('How much does y need to be shifted by to move Curve 2 onto Curve 1?\nSample responses: 15, -20 (no quotes).  Response: ')
        theta = input('How many degrees does Curve 2 need to be rotated in order to align\nit with Curve 1? Sample responses: 45, -30.\nNote that a positive rotation goes counterclockwise.  Response: ')
        fval_prev = 100;
        best_match = tongue_matcher2(C1,C2,theta,xs,ys,fval_prev);
    end
else  
    if fval < fval_prev
        best_match = tongue_matcher2(C1,C2,x(1),x(2),x(3),fval);
    else
        best_match = tongue_matcher2(C1,C2,theta,xs,ys,fval_prev);
    end
end
    
    





